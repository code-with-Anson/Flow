import React, { useState } from 'react';
import { Bubble, Sender, XProvider } from '@ant-design/x';
import { UserOutlined, RobotOutlined } from '@ant-design/icons';
import { XMarkdown } from '@ant-design/x-markdown';
import '@ant-design/x-markdown/es/XMarkdown/index.css'; // Import CSS
import MainLayout from '../components/layout/MainLayout';
import GlassPanel from '../components/ui/GlassPanel';
import './Chat.css';

const roles = {
  user: {
    placement: 'end',
    avatar: { icon: <UserOutlined />, style: { backgroundColor: '#87d068' } },
  },
  ai: {
    placement: 'start',
    avatar: { icon: <RobotOutlined />, style: { backgroundColor: '#f56a00' } },
    typing: { step: 5, interval: 20 },
    style: { maxWidth: 600 },
  },
};

const Chat = () => {
  const [content, setContent] = useState('');
  const [items, setItems] = useState([
    {
      key: '1',
      role: 'ai',
      content: 'Hello! I am Flow AI. How can I help you today?',
    },
  ]);

  // Process items: convert AI content to XMarkdown
  const processedItems = items.map(item => {
    if (item.role === 'ai') {
      return {
        ...item,
        content: <XMarkdown content={item.content} />,
      };
    }
    return item;
  });

  const onSubmit = async (value) => {
    const newItems = [
      ...items,
      {
        key: Date.now().toString(),
        role: 'user',
        content: value,
      },
    ];
    setItems(newItems);
    setContent('');

    // Create a placeholder for AI response
    const aiMsgId = (Date.now() + 1).toString();
    const aiMsgPlaceholder = {
      key: aiMsgId,
      role: 'ai',
      content: '', // Start empty
      loading: true,
    };
    setItems(prev => [...prev, aiMsgPlaceholder]);

    try {
      const token = localStorage.getItem('token');
      const response = await fetch('/api/ai/stream', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'token': token ? `Bearer ${token}` : '',
        },
        body: JSON.stringify({ message: value }),
      });

      if (!response.ok) {
        throw new Error(response.statusText);
      }

      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let aiContent = '';

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;
        
        const chunk = decoder.decode(value, { stream: true });
        // Clean up SSE format (data: prefix) if necessary, but Flux<String> usually returns raw strings in chunks
        // If it returns "data: ...", we need to parse it. 
        // Assuming raw string stream based on Flux<String> return type.
        // Wait, Flux<String> over HTTP usually implies SSE (data: ... \n\n) or just chunked transfer.
        // Let's assume raw text for now, or handle "data:" prefix if it appears.
        
        const lines = chunk.split('\n');
        for (const line of lines) {
            if (line.startsWith('data:')) {
                aiContent += line.replace('data:', '');
            } else {
                aiContent += line;
            }
        }

        // Update UI - store raw text in content
        setItems(prev => prev.map(item => 
          item.key === aiMsgId 
            ? { ...item, content: aiContent, loading: false } 
            : item
        ));
      }

    } catch (error) {
      console.error('Chat error:', error);
      setItems(prev => prev.map(item => 
        item.key === aiMsgId 
          ? { ...item, content: 'Error: Failed to get response.', loading: false } 
          : item
      ));
    }
  };

  return (
    <MainLayout>
      <div className="chat-container">
        <GlassPanel className="chat-panel">
          <XProvider>
            <div className="chat-messages">
              <Bubble.List 
                items={processedItems} 
                roles={roles} 
                autoScroll={false}
                style={{ display: 'flex', flexDirection: 'column' }}
              />
            </div>
            <div className="chat-input-area">
              <Sender
                value={content}
                onChange={setContent}
                onSubmit={onSubmit}
                placeholder="Ask Flow AI anything..."
                className="glass-sender"
              />
            </div>
          </XProvider>
        </GlassPanel>
      </div>
    </MainLayout>
  );
};

export default Chat;
