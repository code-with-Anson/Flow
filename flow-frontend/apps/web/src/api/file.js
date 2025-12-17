import axios from './axios';

/**
 * Upload a file
 * @param {File} file 
 * @returns {Promise<Object>} File entity
 */
export const uploadFile = async (file) => {
    const formData = new FormData();
    formData.append('file', file);
    const res = await axios.post('/file/upload', formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    });
    return res;
};

/**
 * List files
 */
export const listFiles = () => {
    return axios.get('/file/list');
};

/**
 * Delete file
 * @param {string|number} id 
 */
export const deleteFile = (id) => {
    return axios.delete(`/file/${id}`);
};

/**
 * Get download/preview url
 * @param {string|number} id 
 */
export const getFileUrl = (id) => {
    return axios.get(`/file/download/${id}`);
};
