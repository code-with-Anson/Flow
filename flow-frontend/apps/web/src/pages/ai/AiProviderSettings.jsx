import React, { useState, useEffect } from 'react';
import { Card, Button, List, Typography, Modal, Space, Tag, Popconfirm, message, Empty } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, ApiOutlined } from '@ant-design/icons';
import { listProviders, deleteProvider } from '../../api/aiProvider';
import ProviderForm from '../../components/ai/ProviderForm';
import MainLayout from '../../components/layout/MainLayout';
import { useTranslation } from 'react-i18next';

const { Text, Title } = Typography;

const AiProviderSettings = () => {
    const { t } = useTranslation();
    const [providers, setProviders] = useState([]);
    const [loading, setLoading] = useState(false);
    const [modalOpen, setModalOpen] = useState(false);
    const [editingProvider, setEditingProvider] = useState(null);

    useEffect(() => {
        fetchProviders();
    }, []);

    const fetchProviders = async () => {
        try {
            setLoading(true);
            const res = await listProviders();
            if (res?.code === 200) {
                setProviders(res.data || []);
            }
        } catch (err) {
            console.error(err);
            message.error('加载供应商列表失败');
        } finally {
            setLoading(false);
        }
    };

    const handleAdd = () => {
        setEditingProvider(null);
        setModalOpen(true);
    };

    const handleEdit = (item) => {
        setEditingProvider(item);
        setModalOpen(true);
    };

    const handleDelete = async (id) => {
        try {
            const res = await deleteProvider(id);
            if (res?.code === 200) {
                message.success('删除成功');
                fetchProviders();
            } else {
                message.error(res?.message || '删除失败');
            }
        } catch (err) {
            console.error(err);
            message.error('删除失败');
        }
    };

    const handleFormSuccess = () => {
        setModalOpen(false);
        setEditingProvider(null);
        fetchProviders();
    };

    const getProviderColor = (provider) => {
        const colors = {
            'OPENAI': 'green',
            'OLLAMA': 'blue',
            'ANTHROPIC': 'purple',
            'GEMINI': 'orange',
        };
        return colors[provider] || 'default';
    };

    return (
        <MainLayout>
            <div style={{ padding: '24px', maxWidth: 900, margin: '0 auto' }}>
                <Card
                    title={
                        <Space>
                            <ApiOutlined />
                            <span>AI 供应商设置</span>
                        </Space>
                    }
                    extra={
                        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
                            添加供应商
                        </Button>
                    }
                >
                    <List
                        loading={loading}
                        dataSource={providers}
                        locale={{
                            emptyText: (
                                <Empty
                                    description="暂无供应商配置"
                                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                                >
                                    <Button type="primary" onClick={handleAdd}>
                                        添加第一个供应商
                                    </Button>
                                </Empty>
                            )
                        }}
                        renderItem={(item) => (
                            <List.Item
                                style={{
                                    background: '#fafafa',
                                    borderRadius: 8,
                                    marginBottom: 12,
                                    padding: '16px 20px',
                                }}
                                actions={[
                                    <Button
                                        type="text"
                                        icon={<EditOutlined />}
                                        onClick={() => handleEdit(item)}
                                    >
                                        编辑
                                    </Button>,
                                    <Popconfirm
                                        title="确定删除此供应商配置吗？"
                                        onConfirm={() => handleDelete(item.id)}
                                        okText="确定"
                                        cancelText="取消"
                                    >
                                        <Button type="text" danger icon={<DeleteOutlined />}>
                                            删除
                                        </Button>
                                    </Popconfirm>
                                ]}
                            >
                                <List.Item.Meta
                                    title={
                                        <Space>
                                            <Text strong style={{ fontSize: 16 }}>{item.name}</Text>
                                            <Tag color={getProviderColor(item.provider)}>
                                                {item.provider}
                                            </Tag>
                                            {item.enabled ? (
                                                <Tag color="success">已启用</Tag>
                                            ) : (
                                                <Tag color="default">已禁用</Tag>
                                            )}
                                        </Space>
                                    }
                                    description={
                                        <Space direction="vertical" size={4} style={{ marginTop: 8 }}>
                                            <Text type="secondary">
                                                API 地址: {item.baseUrl}
                                            </Text>
                                            {item.models && (
                                                <Text type="secondary">
                                                    可用模型: {item.models}
                                                </Text>
                                            )}
                                        </Space>
                                    }
                                />
                            </List.Item>
                        )}
                    />
                </Card>

                <Modal
                    title={editingProvider ? '编辑供应商' : '添加供应商'}
                    open={modalOpen}
                    onCancel={() => {
                        setModalOpen(false);
                        setEditingProvider(null);
                    }}
                    footer={null}
                    destroyOnClose
                >
                    <ProviderForm
                        initialValues={editingProvider}
                        onSuccess={handleFormSuccess}
                        onCancel={() => setModalOpen(false)}
                    />
                </Modal>
            </div>
        </MainLayout>
    );
};

export default AiProviderSettings;
