import http from '../axios'

export default {
  queryPage(data) {
    return http({
      url: '/api/shortlink/project/getpagelink',
      method: 'get',
      params: data
    })
  },
  addSmallLink(data) {
    return http({
      url: '/api/shortlink/project/link/create',
      method: 'post',
      data
    })
  },
  addLinks(data) {
    return http({
      responseType: 'arraybuffer',
      url: '/api/shortlink/project/link/create/batch',
      method: 'post',
      data,
      // responseType: 'blob'
    })
  },
  editSmallLink(data) {
    return http({
      url: '/api/shortlink/project/link/updatebase',
      method: 'put',
      data
    })
  },
  // 通过链接查询标题
  queryTitle(data) {
    return http({
      method: 'get',
      url: '/api/shortlink/project/link/title',
      params: data
    })
  },
  // 移动到回收站（创建）
  toRecycleBin(data) {
    return http({
      url: '/api/shortlink/project/recyclelink/create',
      method: 'put',
      data
    })
  },
  // 查询回收站数据（分页）
  queryRecycleBin({ current = 1, size = 10 } = {}) {
    return http({
      url: '/api/shortlink/project/recyclelink/getpage',
      method: 'get',
      params: { current, size }
    })
  },
  // 恢复短链接
  recoverLink(data) {
    return http({
      url: '/api/shortlink/project/recyclelink/recover',
      method: 'put',
      data
    })
  },
  // 删除短链接
  removeLink(data) {
    return http({
      url: '/api/shortlink/project/recyclelink/delete',
      method: 'delete',
      data
    })
  },
  // 查询单链的图表数据
  queryLinkStats(data) {
    return http({
      method: 'get',
      params: data,
      url: '/api/shortlink/project/stats'
    })
  },
  // 查询分组的访问记录
  queryLinkTable(data) {
    return http({
      method: 'get',
      params: data,
      url: '/api/shortlink/project/stats/access-record'
    })
  }
}
