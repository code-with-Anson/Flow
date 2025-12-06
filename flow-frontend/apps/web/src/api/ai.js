import axios from './axios';

// 获取所有对话
export const listConversations = () => {
    return axios.get('/ai/conversation');
};

// 创建新对话
export const createConversation = (title) => {
    return axios.post('/ai/conversation', null, {
        params: { title }
    });
};

// 获取对话消息详情
export const getConversationMessages = (id) => {
    return axios.get(`/ai/conversation/${id}`);
};

// 更新对话标题
export const updateConversationTitle = (id, title) => {
    return axios.put(`/ai/conversation/${id}/title`, null, {
        params: { title }
    });
};

// 删除对话
export const deleteConversation = (id) => {
    return axios.delete(`/ai/conversation/${id}`);
};

/**
 * 流式对话 (使用 fetch 实现 SSE)
 * 
 * SSE 格式说明：
 * - 每个事件以 \n\n 分隔
 * - 一个事件可以有多个 data: 行，它们应该用换行符连接
 * - 例如：后端发送 "hello\nworld" 会变成：
 *   data: hello
 *   data: world
 *   
 *   (空行表示事件结束)
 */
export const streamChat = async (data, onChunk, onFinish, onError) => {
    const token = localStorage.getItem('token');
    try {
        const response = await fetch('/api/ai/stream', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'token': token ? `Bearer ${token}` : '',
            },
            body: JSON.stringify(data),
        });

        if (!response.ok) {
            if (response.status === 401) {
                localStorage.removeItem('token');
                window.location.href = '/login';
                return;
            }
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        let buffer = ''; // 缓存不完整的 SSE 数据

        while (true) {
            const { done, value } = await reader.read();
            if (done) {
                // 处理缓冲区中剩余的数据
                if (buffer.trim()) {
                    const content = parseSSEEvent(buffer);
                    if (content && onChunk) onChunk(content);
                }
                if (onFinish) onFinish();
                break;
            }
            
            // 解码并追加到缓冲区
            buffer += decoder.decode(value, { stream: true });
            
            // SSE 事件以 \n\n 分隔
            const events = buffer.split('\n\n');
            // 最后一个可能不完整，保留在缓冲区
            buffer = events.pop() || '';
            
            // 处理每个完整的事件
            for (const event of events) {
                if (!event.trim()) continue;
                
                const content = parseSSEEvent(event);
                if (content && onChunk) {
                    onChunk(content);
                }
            }
        }
    } catch (error) {
        console.error("Stream chat error:", error);
        if (onError) onError(error);
    }
};

/**
 * 解析单个 SSE 事件
 * 一个事件中的多个 data: 行应该用换行符连接
 */
function parseSSEEvent(event) {
    const lines = event.split('\n');
    const dataLines = [];
    
    for (const line of lines) {
        if (line.startsWith('data:')) {
            // 去掉 'data:' 前缀和可能的前导空格
            let content = line.slice(5);
            if (content.startsWith(' ')) {
                content = content.slice(1);
            }
            dataLines.push(content);
        }
        // 忽略其他行（如 event:, id:, retry: 等）
    }
    
    // 多个 data 行用换行符连接（这是 SSE 标准）
    return dataLines.join('\n');
}
