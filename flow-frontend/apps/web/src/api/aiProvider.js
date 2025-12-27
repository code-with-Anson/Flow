import axios from './axios';

/**
 * AI 供应商管理 API
 */

// 获取所有供应商
export const listProviders = () => {
    return axios.get('/ai/provider');
};

// 添加/更新供应商
export const saveOrUpdateProvider = (data) => {
    return axios.post('/ai/provider', data);
};

// 删除供应商
export const deleteProvider = (id) => {
    return axios.delete(`/ai/provider/${id}`);
};
