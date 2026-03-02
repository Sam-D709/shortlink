import http from '../axios'

export default {
  // 查询分组集合
  queryGroup(data) {
    return http({
      url: '/api/shortlink/admin/group/list',
      method: 'get',
      params: data
    })
  },
  // 新增短链分组
  addGroup(groupName) {
    return http({
      url: '/api/shortlink/admin/group/create',
      method: 'post',
      params: { groupName }
    })
  },
  // 修改短链分组
  editGroup(data) {
    return http({
      url: '/api/shortlink/admin/group/updatename',
      method: 'put',
      params: { gid: data.gid, groupName: data.name }
    })
  },
  // 删除短链分组
  deleteGroup(data) {
    return http({
      url: '/api/shortlink/admin/group/delete',
      method: 'delete',
      params: data
    })
  },
  sortGroup(data) {
    return http({
      url: '/api/shortlink/admin/group/order',
      method: 'post',
      data
    })
  },
  // 查询分组的图表数据
  queryGroupStats(data) {
    return http({
      method: 'get',
      params: data,
      url: '/api/shortlink/admin/stats/group'
    })
  },
  // 查询分组的访问记录
  queryGroupTable(data) {
    return http({
      method: 'get',
      params: data,
      url: '/api/shortlink/admin/stats/access-record/group'
    })
  }
}