import React, { useState, useEffect, useRef } from 'react';
import { Layout, List, Button, Typography, message, theme } from 'antd';
import { PlusOutlined, DeleteOutlined, MessageOutlined } from '@ant-design/icons';
import { Bubble, Sender } from '@ant-design/x';
import { XMarkdown } from '@ant-design/x-markdown';
import '@ant-design/x-markdown/es/XMarkdown/index.css';
import { 
    listConversations, 
    createConversation, 
    getConversationMessages, 
    deleteConversation, 
    streamChat 
} from '../../api/ai';
import MainLayout from '../../components/layout/MainLayout';


const { Sider, Content } = Layout;
const { Text } = Typography;

const AiChatPage = () => {
    const { token } = theme.useToken();
    
    // State
    const [conversations, setConversations] = useState([]);
    const [currentConversationId, setCurrentConversationId] = useState(null);
    const [messages, setMessages] = useState([]); // Array of { role, content, id }
    const [loading, setLoading] = useState(false);
    const [inputValue, setInputValue] = useState('');
    const [streamingContent, setStreamingContent] = useState(''); // Current incomplete AI response
    const messagesEndRef = useRef(null);
    const chatContainerRef = useRef(null);

    // Scroll to bottom - 使用 setTimeout 确保在渲染完成后滚动
    const scrollToBottom = () => {
        setTimeout(() => {
            if (chatContainerRef.current) {
                chatContainerRef.current.scrollTop = chatContainerRef.current.scrollHeight;
            }
        }, 50);
    };

    // Load conversations on mount
    useEffect(() => {
        fetchConversations();
    }, []);

    // Auto-scroll when messages or streaming content changes
    useEffect(() => {
        scrollToBottom();
    }, [messages, streamingContent]);

    // Load messages when conversation changes
    useEffect(() => {
        if (currentConversationId) {
            fetchMessages(currentConversationId);
        } else {
            setMessages([]);
        }
    }, [currentConversationId]);

    const fetchConversations = async () => {
        try {
            const res = await listConversations();
            setConversations(res || []);
            // Optional: Auto-select first conversation
            // if (res && res.length > 0 && !currentConversationId) {
            //     setCurrentConversationId(res[0].id);
            // }
        } catch (err) {
            console.error(err);
            message.error('加载会话列表失败');
        }
    };

    const fetchMessages = async (id) => {
        try {
            setLoading(true);
            const res = await getConversationMessages(id);
            // Backend returns ASC (oldest first)
            // 保持原始字符串，在 renderBubbles 中统一处理 markdown 渲染
            const mapped = (res || []).map(msg => ({
                key: msg.id,
                role: msg.role,
                content: msg.content,
            }));
            setMessages(mapped);
        } catch (err) {
            console.error(err);
            message.error('加载消息失败');
        } finally {
            setLoading(false);
        }
    };
    
    // ... (rest of code)

    // Render Items for Bubble.List
    const renderBubbles = () => {
        // 统一在渲染时处理 markdown，AI 消息用 XMarkdown 包装
        const items = messages.map(msg => ({
            key: msg.key,
            role: msg.role,
            content: msg.role === 'ai' ? <XMarkdown content={msg.content} /> : msg.content,
            typing: false,
        }));

        if (streamingContent) {
            items.push({
                key: 'streaming',
                role: 'ai',
                content: <XMarkdown content={streamingContent} />,
                typing: { step: 5, interval: 20 },
                // streaming 告诉 Bubble 当前内容还在流式输入中
                // 这样可以避免在内容未完全到达时触发 onTypingComplete
                streaming: true,
            });
        }
        
        return items;
    };

    const handleNewConversation = async () => {
        try {
            const res = await createConversation('新对话');
            setConversations([res, ...conversations]);
            setCurrentConversationId(res.id);
            setMessages([]);
        } catch (err) {
            console.error(err);
            message.error('创建会话失败');
        }
    };

    const handleDeleteConversation = async (e, id) => {
        e.stopPropagation();
        try {
            await deleteConversation(id);
            setConversations(conversations.filter(c => c.id !== id));
            if (currentConversationId === id) {
                setCurrentConversationId(null);
                setMessages([]);
            }
            message.success('删除成功');
        } catch (err) {
            console.error(err);
            message.error('删除失败');
        }
    };

    const handleSubmit = (content) => {
        if (!content) return;
        
        // Optimistic update for User message
        const userMsg = { key: Date.now(), role: 'user', content };
        setMessages(prev => [...prev, userMsg]);
        setInputValue('');
        setLoading(true);

        // Prepare request
        const reqData = {
            conversationId: currentConversationId,
            message: content,
            model: 'gemini-2.5-flash' // Default
        };

        // If no conversation selected, backend might need to handle creation or we enforce creation first.
        // Current logic: backend creates if ID missing but returns ID in stream? 
        // No, current backend assumes ID passed if meaningful persistence.
        // Frontend logic adjustment: If no conversation, create one first or handle dynamically.
        // For simplicity: If !currentConversationId, create first.
        
        const ensureConversation = async () => {
            let targetId = currentConversationId;
            if (!targetId) {
                try {
                    const newConv = await createConversation(content.slice(0, 10)); // Use first chars as title
                    targetId = newConv.id;
                    setConversations([newConv, ...conversations]);
                    setCurrentConversationId(targetId);
                    reqData.conversationId = targetId; // Update req
                } catch (e) {
                    console.error(e);
                    message.error("无法创建会话");
                    return null;
                }
            }
            return targetId;
        };

        ensureConversation().then(targetId => {
            if (!targetId) {
                setLoading(false);
                return;
            }

            let fullAiResponse = "";
            setStreamingContent("AI 正在思考..."); // Placeholder or empty
            
            streamChat(
                reqData,
                (chunk) => {
                    // On Chunk
                    setLoading(true); // Keep loading state distinct if needed, or use streamingContent
                    fullAiResponse += chunk;
                    setStreamingContent(fullAiResponse);
                },
                () => {
                    // On Finish
                    const aiMsg = { key: Date.now() + 1, role: 'ai', content: fullAiResponse };
                    setMessages(prev => [...prev, aiMsg]);
                    setStreamingContent('');
                    setLoading(false);
                    // Refresh conversations to update order/time
                    // fetchConversations(); // Optional, might cause flicker 
                },
                (err) => {
                    console.error(err);
                    message.error("AI 响应失败");
                    setLoading(false);
                    setStreamingContent('');
                }
            );
        });
    };

    // Render Items for Bubble.List


    return (
        <MainLayout>
            <Layout style={{ height: 'calc(100vh - 64px)', background: token.colorBgLayout }}>
                <Sider 
                    width={280} 
                    theme="light" 
                    style={{ 
                        borderRight: `1px solid ${token.colorBorderSecondary}`,
                        overflowY: 'auto'
                    }}
                >
                    <div style={{ padding: 16 }}>
                        <Button type="primary" block icon={<PlusOutlined />} onClick={handleNewConversation}>
                            新对话
                        </Button>
                    </div>
                    <List
                        itemLayout="horizontal"
                        dataSource={conversations}
                        renderItem={(item) => (
                            <List.Item 
                                style={{ 
                                    cursor: 'pointer', 
                                    padding: '12px 16px',
                                    background: currentConversationId === item.id ? token.colorBgContainerDisabled : 'transparent'
                                }}
                                onClick={() => setCurrentConversationId(item.id)}
                                actions={[
                                    <DeleteOutlined key="delete" onClick={(e) => handleDeleteConversation(e, item.id)} />
                                ]}
                            >
                                <List.Item.Meta
                                    avatar={<MessageOutlined />}
                                    title={
                                        <Text ellipsis style={{ width: 140 }}>
                                            {item.title}
                                        </Text>
                                    }
                                    description={<Text type="secondary" style={{ fontSize: 12 }}>{item.model}</Text>}
                                />
                            </List.Item>
                        )}
                    />
                </Sider>
                <Content style={{ position: 'relative', display: 'flex', flexDirection: 'column' }}>
                     <div ref={chatContainerRef} style={{ flex: 1, overflowY: 'auto', padding: '24px 24px 80px 24px' }}>
                        <Bubble.List 
                            items={renderBubbles()}
                            autoScroll={false}
                            roles={{
                             ai: { 
                                placement: 'start', 
                                avatar: { src: 'https://api.dicebear.com/7.x/bottts/svg?seed=Sakura', style: { backgroundColor: '#f56a00' } },
                                style: { maxWidth: 600, marginInlineEnd: 48 }, // Better styling
                             },
                             user: { 
                                placement: 'end', 
                                avatar: { src: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Anson', style: { backgroundColor: '#87d068' } } 
                             }
                        }}
                        />
                        <div ref={messagesEndRef} />
                     </div>
                     
                     <div style={{ 
                         padding: 24, 
                         borderTop: `1px solid ${token.colorBorderSecondary}`,
                         background: token.colorBgContainer
                     }}>
                         <Sender 
                             value={inputValue}
                             onChange={(v) => setInputValue(v)}
                             onSubmit={handleSubmit}
                             loading={loading}
                             placeholder="给樱和晓发送消息..."
                         />
                     </div>
                </Content>
            </Layout>
        </MainLayout>
    );
};

export default AiChatPage;
