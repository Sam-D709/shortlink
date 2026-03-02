package org.samd.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.samd.shortlink.admin.common.conversion.errorcode.UserErrorCode;
import org.samd.shortlink.admin.common.conversion.exception.ClientException;
import org.samd.shortlink.admin.common.conversion.exception.ServiceException;
import org.samd.shortlink.admin.common.util.ParamValidator;
import org.samd.shortlink.admin.common.util.UserContext;
import org.samd.shortlink.admin.dao.entity.UserDO;
import org.samd.shortlink.admin.dao.mapper.UserMapper;
import org.samd.shortlink.admin.dto.req.UserLoginReqDTO;
import org.samd.shortlink.admin.dto.req.UserRegisterReqDTO;
import org.samd.shortlink.admin.dto.req.UserUpdateReqDTO;
import org.samd.shortlink.admin.dto.resp.UserLoginRespDTO;
import org.samd.shortlink.admin.dto.resp.UserRespDTO;
import org.samd.shortlink.admin.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.samd.shortlink.admin.common.conversion.errorcode.UserErrorCode.USER_EXIST;
import static org.samd.shortlink.admin.constant.RedisCacheConstant.LOCK_USER_REGISTER_KEY;
import static org.samd.shortlink.admin.constant.RedisCacheConstant.USER_LOGIN_KEY;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public UserRespDTO getUserByUsername() {
        String username = UserContext.getUsername();
        QueryWrapper<UserDO> qw = new QueryWrapper<>();
        qw.eq("username", username);
        UserDO userDO = getOne(qw);
        ParamValidator.objNonNull(userDO);
        log.info("查到的 userDO 为:{}", userDO);
        UserRespDTO dto = new UserRespDTO();
        BeanUtils.copyProperties(userDO, dto);
        log.info("转换得到的 dto 为:{}", dto);
        return dto;
    }

    @Override
    public Boolean hasUsername(String username) {
        ParamValidator.NameNotNull(username);
        log.info("检查用户名 {} 是否存在布隆过滤器中", username);
        return userRegisterCachePenetrationBloomFilter.contains(username);
    }

    @Transactional
    @Override
    public Boolean registerUser(UserRegisterReqDTO requestParam) {
        ParamValidator.checkUserRegisterReqDTOValuable(requestParam);
        String username = requestParam.getUsername().trim();
        if (hasUsername(username)) {
            throw new ServiceException(USER_EXIST);
        }
        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER_KEY + username);
        boolean locked = false;
        try {
            // 尝试获取锁（带超时），避免无限等待
            log.info("尝试获取用户注册锁:{}", LOCK_USER_REGISTER_KEY + username);
            locked = lock.tryLock();
            if (!locked) {
                throw new ClientException(UserErrorCode.USER_EXIST);
            }
            UserDO userDO = new UserDO();
            BeanUtils.copyProperties(requestParam, userDO);
            log.info("准备保存的 userDO 为:{}", userDO);
            if (!save(userDO)) {
                throw new ServiceException(UserErrorCode.USER_SAVE_ERROR);
            }
            userRegisterCachePenetrationBloomFilter.add(username);
            log.info("布隆过滤器添加用户名 {} 成功", username);
            return true;
        } catch (DuplicateKeyException ex) {
            throw new ClientException(USER_EXIST);
        } finally {
            if (locked) {
                log.info("释放用户注册锁:{}", LOCK_USER_REGISTER_KEY + username);
                lock.unlock();
            }
        }
    }

    // java
    @Override
    public Boolean updateUser(UserUpdateReqDTO requestParam) {
        UserRegisterReqDTO dto = new UserRegisterReqDTO();
        BeanUtils.copyProperties(requestParam, dto);
        ParamValidator.checkUserRegisterReqDTOValuable(dto);
        log.info("更新用户信息实体参数有效");
        String username = UserContext.getUsername();
        UpdateWrapper<UserDO> uw = new UpdateWrapper<>();
        uw.eq("username", username);
        if(update(BeanUtil.toBean(requestParam, UserDO.class), uw)){
            return true;
        }else{
            throw new ServiceException(UserErrorCode.USER_SAVE_ERROR);
        }
    }

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParam) {
        ParamValidator.NameNotNull(requestParam.getUsername());
        ParamValidator.passwordNotNull(requestParam.getPassword());
        QueryWrapper<UserDO> qw = new QueryWrapper<>();
        qw.eq("username", requestParam.getUsername())
                .eq("password", requestParam.getPassword())
                .eq("delflag", 0);
        UserDO userDO = getOne(qw);
        if (userDO == null) {
            throw new ClientException("用户不存在");
        }
        String loginKey = USER_LOGIN_KEY + requestParam.getUsername();
        Long ttl = stringRedisTemplate.getExpire(loginKey, TimeUnit.SECONDS);
        // ttl > 0: key exists and not expired
        if (ttl > 0) {
            Map<Object, Object> hasLoginMap = stringRedisTemplate.opsForHash().entries(loginKey);
            if (CollUtil.isNotEmpty(hasLoginMap)) {
                // 刷新会话过期时间
                stringRedisTemplate.expire(loginKey, 30L, TimeUnit.DAYS);
                String token = hasLoginMap.keySet().stream()
                        .findFirst()
                        .map(Object::toString)
                        .orElseThrow(() -> new ClientException("用户登录错误"));
                return new UserLoginRespDTO(token);
            }
        }
        String uuid = UUID.randomUUID().toString();
        stringRedisTemplate.opsForHash().put(loginKey, uuid, JSONUtil.toJsonStr(userDO));
        stringRedisTemplate.expire(loginKey, 30L, TimeUnit.DAYS);
        return new UserLoginRespDTO(uuid);
    }

    @Override
    public Boolean logout() {
        String username = UserContext.getUsername();
        String loginKey = USER_LOGIN_KEY + username;
        Map<Object, Object> tokenMap = stringRedisTemplate.opsForHash().entries(loginKey);
        if (CollUtil.isNotEmpty(tokenMap)) {
            for (Object token : tokenMap.keySet()) {
                stringRedisTemplate.opsForHash().delete(loginKey, token);
            }
            stringRedisTemplate.delete(loginKey);
            return true;
        }
        throw new ClientException("用户 Token不存在或用户未登录");
    }
}
