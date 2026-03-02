import http from '../axios'

export default {
  // 注册
  addUser(data) {
    return http({
      url: '/api/shortlink/admin/user/register',
      method: 'post',
      data
    })
  },
  // 编辑信息
  editUser(data) {
    return http({
      url: '/api/shortlink/admin/user',
      method: 'put',
      data
    })
  },
  // 登录
  login(data) {
    return http({
      url: '/api/shortlink/admin/user/login',
      method: 'post',
      data
    })
  },
  // 退出登录
  logout() {
    return http({
      url: '/api/shortlink/admin/user/logout',
      method: 'post'
    })
  },
  // 检查用户名是否可用
  hasUsername(data) {
    return http({
      url: '/api/shortlink/admin/user/hasUsername',
      method: 'get',
      params: data
    })
  },
  // 根据用户名查找用户信息（实际应为获取当前登录用户信息）
  queryUserInfo() {
    return http({
      url: '/api/shortlink/admin/user/username',
      method: 'get'
    })
  }
}
