import React, { useState, useEffect, useRef } from 'react';
import { Layout, List, Button, Typography, message, theme, Switch, Upload } from 'antd';
import { PlusOutlined, DeleteOutlined, MessageOutlined, CloudUploadOutlined, SettingOutlined } from '@ant-design/icons';
import { Bubble, Sender } from '@ant-design/x';
import { XMarkdown } from '@ant-design/x-markdown';
import '@ant-design/x-markdown/es/XMarkdown/index.css';
import { 
    listConversations, 
    createConversation, 
    getConversationMessages, 
    deleteConversation, 
    streamChat,
    ingestFile
} from '../../api/ai';
import { uploadFile } from '../../api/file';
import { useTranslation } from 'react-i18next';
import MainLayout from '../../components/layout/MainLayout';
import ProviderSelector from '../../components/ai/ProviderSelector';


const { Sider, Content } = Layout;
const { Text } = Typography;

const AiChatPage = () => {
    const { token } = theme.useToken();
    const { t } = useTranslation();
    
    // State
    const [conversations, setConversations] = useState([]);
    const [currentConversationId, setCurrentConversationId] = useState(null);
    const [messages, setMessages] = useState([]); // Array of { role, content, id }
    const [loading, setLoading] = useState(false);
    const [inputValue, setInputValue] = useState('');
    const [useKnowledgeBase, setUseKnowledgeBase] = useState(false); // RAG Toggle
    const [streamingContent, setStreamingContent] = useState(''); // Current incomplete AI response
    const [selectedProviderId, setSelectedProviderId] = useState(null); // AI 供应商ID
    const [selectedModel, setSelectedModel] = useState(''); // 选中的模型
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
            providerId: selectedProviderId, // 使用选中的供应商
            model: selectedModel, // 使用选中的模型
            useKnowledgeBase: useKnowledgeBase // Pass RAG toggle state
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
            <div style={{ 
                display: 'flex', 
                height: 'calc(100vh - 80px)', 
                background: '#fff', 
                overflow: 'hidden' 
            }}>
                {/* Custom Sidebar */}
                <div style={{ 
                    width: '280px', 
                    // Remove border, use subtle background for distinction
                    background: '#fbfbfb',
                    display: 'flex', 
                    flexDirection: 'column',
                }}>
                    <div style={{ padding: '20px' }}>
                        <Button 
                            type="primary" 
                            shape="round" 
                            block 
                            icon={<PlusOutlined />} 
                            onClick={handleNewConversation}
                            style={{ boxShadow: 'none' }}
                        >
                            {t('aiChat.newConversation')}
                        </Button>
                    </div>
                    <div style={{ flex: 1, overflowY: 'auto' }}>
                        <List
                            itemLayout="horizontal"
                            dataSource={conversations}
                            split={false}
                            renderItem={(item) => (
                                <List.Item 
                                    style={{ 
                                        cursor: 'pointer', 
                                        padding: '12px 20px',
                                        transition: 'all 0.2s',
                                        background: currentConversationId === item.id ? '#fff' : 'transparent',
                                        borderLeft: currentConversationId === item.id ? '3px solid #1677ff' : '3px solid transparent'
                                    }}
                                    className="conversation-item"
                                    onClick={() => setCurrentConversationId(item.id)}
                                    actions={[
                                        <div className="delete-icon" style={{ opacity: 0.5, transition: 'opacity 0.2s' }}>
                                             <DeleteOutlined onClick={(e) => handleDeleteConversation(e, item.id)} />
                                        </div>
                                    ]}
                                >
                                    <List.Item.Meta
                                        avatar={<MessageOutlined style={{ color: currentConversationId === item.id ? '#1677ff' : '#999' }} />}
                                        title={
                                            <Text ellipsis style={{ width: 140, fontWeight: currentConversationId === item.id ? 600 : 400 }}>
                                                {item.title}
                                            </Text>
                                        }
                                    />
                                </List.Item>
                            )}
                        />
                    </div>
                </div>

                {/* Main Chat Area */}
                <div style={{ flex: 1, display: 'flex', flexDirection: 'column', position: 'relative' }}>
                     {/* 顶部工具栏: 供应商选择器 */}
                     <div style={{ 
                         padding: '12px 10%', 
                         borderBottom: '1px solid #f0f0f0',
                         display: 'flex',
                         justifyContent: 'space-between',
                         alignItems: 'center',
                         background: '#fafafa'
                     }}>
                         <ProviderSelector
                             providerId={selectedProviderId}
                             model={selectedModel}
                             onProviderChange={(id) => setSelectedProviderId(id)}
                             onModelChange={(m) => setSelectedModel(m)}
                         />
                         <Button 
                             type="text" 
                             icon={<SettingOutlined />} 
                             onClick={() => window.location.href = '/ai/settings'}
                             size="small"
                         >
                             供应商设置
                         </Button>
                     </div>
                     <div ref={chatContainerRef} style={{ 
                         flex: 1, 
                         overflowY: 'auto', 
                         padding: '40px 10% 240px 10%',
                         scrollBehavior: 'smooth'
                    }}>
                        {messages.length === 0 && !streamingContent ? (
                            <div style={{ 
                                height: '100%', 
                                display: 'flex', 
                                flexDirection: 'column', 
                                justifyContent: 'center', 
                                alignItems: 'center', 
                                color: '#ccc',
                                userSelect: 'none'
                            }}>
                                <MessageOutlined style={{ fontSize: 48, marginBottom: 16, opacity: 0.5 }} />
                                <Text type="secondary">{t('aiChat.startNewChat')}</Text>
                            </div>
                        ) : (
                            <Bubble.List 
                                items={renderBubbles()}
                                autoScroll={false}
                                roles={{
                                    ai: { 
                                        placement: 'start', 
                                        avatar: { src: 'https://api.dicebear.com/7.x/bottts/svg?seed=Sakura', style: { backgroundColor: '#fff', border: '1px solid #eee' } },
                                        style: { 
                                            maxWidth: 800, 
                                            background: '#f7f7f7',
                                            borderRadius: '12px',
                                            padding: '12px 20px',
                                            border: 'none',
                                            boxShadow: 'none'
                                        }
                                    },
                                    user: { 
                                        placement: 'end', 
                                        avatar: { src: 'https://api.dicebear.com/7.x/avataaars/svg?seed=Anson', style: { backgroundColor: '#e6f4ff' } },
                                        style: {
                                            maxWidth: 800,
                                            background: '#1677ff',
                                            color: '#fff',
                                            borderRadius: '12px',
                                            padding: '12px 20px',
                                            border: 'none',
                                            boxShadow: '0 2px 8px rgba(22, 119, 255, 0.15)'
                                        }
                                    }
                                }}
                            />
                        )}
                        <div ref={messagesEndRef} />
                     </div>
                     
                     {/* Input Area */}
                     <div style={{ 
                         position: 'absolute',
                         bottom: 0,
                         left: 0,
                         right: 0,
                         padding: '24px calc(10% + 40px)', 
                         background: 'linear-gradient(to top, #ffffff 80%, rgba(255,255,255,0))',
                     }}>
                     <div style={{ 
                             background: '#fff', 
                             borderRadius: '24px', 
                             boxShadow: '0 4px 20px rgba(0,0,0,0.08)',
                             border: '1px solid #eee',
                             padding: '4px'
                         }}>
                             <Sender 
                                 value={inputValue}
                                 onChange={(v) => setInputValue(v)}
                                 onSubmit={handleSubmit}
                                 loading={loading}
                                 placeholder={t('aiChat.placeholder')}
                                 style={{ border: 'none', background: 'transparent' }}
                             />
                          </div>
                          
                          {/* Knowledge Base Toggle & Tools */}
                          <div style={{ 
                              marginTop: '8px', 
                              display: 'flex', 
                              justifyContent: 'flex-end',
                              alignItems: 'center',
                              paddingRight: '12px',
                              gap: '16px'
                          }}>
                              {/* File Upload for Knowledge Base */}
                              <Upload
                                showUploadList={false}
                                customRequest={async ({ file, onSuccess, onError }) => {
                                  try {
                                    message.loading({ content: '正在上传...', key: 'upload' });
                                    const res = await uploadFile(file);
                                    if(res && res.code === 200) {
                                      const remoteFile = res.data;
                                      message.loading({ content: '正在摄入知识库...', key: 'upload' });
                                      await ingestFile(remoteFile.id, remoteFile.name);
                                      message.success({ content: '已添加到知识库', key: 'upload' });
                                      onSuccess();
                                    } else {
                                      throw new Error(res.msg || '上传失败');
                                    }
                                  } catch (err) {
                                    console.error(err);
                                    message.error({ content: '上传/处理失败', key: 'upload' });
                                    onError(err);
                                  }
                                }}
                              >
                                <Button size="small" icon={<CloudUploadOutlined />} type="text">
                                  {t('aiChat.uploadMaterial')}
                                </Button>
                              </Upload>

                              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                  <Text type="secondary" style={{ fontSize: '12px' }}>{t('aiChat.enableKnowledgeBase')}</Text>
                                  <Switch 
                                      size="small" 
                                      checked={useKnowledgeBase} 
                                      onChange={setUseKnowledgeBase} 
                                      style={{ background: useKnowledgeBase ? '#f472b6' : undefined }}
                                  />
                              </div>
                          </div>
                     </div>
                </div>
            </div>
        </MainLayout>
    );
};

export default AiChatPage;



