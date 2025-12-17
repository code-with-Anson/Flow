import React, { useState, useEffect } from 'react';
import { Table, Button, Tag, Space, Modal, message, Upload, Typography, Tooltip } from 'antd';
import { 
    CloudUploadOutlined, 
    DeleteOutlined, 
    EyeOutlined, 
    FileOutlined, 
    FileImageOutlined, 
    FileTextOutlined, 
    VideoCameraOutlined 
} from '@ant-design/icons';
import { useTranslation } from 'react-i18next';
import MainLayout from '../../components/layout/MainLayout';
import { listFiles, deleteFile, uploadFile, getFileUrl } from '../../api/file';
import { ingestFile } from '../../api/ai';

const { Title, Text } = Typography;

const KnowledgeBasePage = () => {
    const { t } = useTranslation();
    const [loading, setLoading] = useState(false);
    const [data, setData] = useState([]);
    
    // fetch data
    const fetchData = async () => {
        setLoading(true);
        try {
            const res = await listFiles();
            if (res && res.code === 200) {
                setData(res.data || []);
            } else {
                message.error(res?.msg || t('knowledgeBase.messages.loadFailed'));
            }
        } catch (err) {
            console.error(err);
            message.error(t('knowledgeBase.messages.loadFailed'));
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
    }, []);

    const handleDelete = (record) => {
        Modal.confirm({
            title: t('knowledgeBase.deleteConfirm.title'),
            content: t('knowledgeBase.deleteConfirm.content', { name: record.name }),
            onOk: async () => {
                try {
                    await deleteFile(record.id);
                    message.success(t('knowledgeBase.messages.deleteSuccess'));
                    fetchData();
                } catch (err) {
                    message.error(t('knowledgeBase.messages.deleteFailed'));
                }
            }
        });
    };

    const handlePreview = async (record) => {
        try {
            const res = await getFileUrl(record.id);
            if (res && res.code === 200 && res.data) {
                window.open(res.data, '_blank');
            } else {
                message.error(res?.msg || t('knowledgeBase.messages.noLink'));
            }
        } catch (err) {
            message.error(t('knowledgeBase.messages.linkFailed'));
        }
    };
    
    // Status Mapping
    const getStatusTag = (status) => {
        switch (status) {
            case 0: return <Tag color="blue">{t('knowledgeBase.status.uploading')}</Tag>;
            case 1: return <Tag color="orange">{t('knowledgeBase.status.processing')}</Tag>;
            case 2: return <Tag color="green">{t('knowledgeBase.status.completed')}</Tag>;
            case -1: return <Tag color="red">{t('knowledgeBase.status.failed')}</Tag>;
            case 3: return <Tag color="cyan">{t('knowledgeBase.status.queued')}</Tag>;
            default: return <Tag>{t('knowledgeBase.status.unknown')}</Tag>;
        }
    };

    // Icon Mapping
    const getFileIcon = (contentType) => {
        if (!contentType) return <FileOutlined />;
        if (contentType.startsWith('image')) return <FileImageOutlined style={{ color: '#eb2f96' }} />;
        if (contentType.startsWith('video')) return <VideoCameraOutlined style={{ color: '#faad14' }} />;
        if (contentType.startsWith('text')) return <FileTextOutlined style={{ color: '#1890ff' }} />;
        return <FileOutlined />;
    };

    const columns = [
        {
            title: '',
            dataIndex: 'type',
            width: 50,
            render: (type) => getFileIcon(type)
        },
        {
            title: t('knowledgeBase.columns.fileName'),
            dataIndex: 'name',
            render: (text) => <Text strong>{text}</Text>
        },
        {
            title: t('knowledgeBase.columns.size'),
            dataIndex: 'size',
            render: (size) => (size / 1024 / 1024).toFixed(2) + ' MB'
        },
        {
            title: t('knowledgeBase.columns.status'),
            dataIndex: 'status',
            render: (status, record) => (
                <Space>
                    {getStatusTag(status)}
                    {status === -1 && record.errorMsg && (
                         <Tooltip title={record.errorMsg}>
                             <Text type="secondary" style={{ fontSize: 12 }}>{t('knowledgeBase.viewReason')}</Text>
                         </Tooltip>
                    )}
                </Space>
            )
        },
        {
            title: t('knowledgeBase.columns.uploadTime'),
            dataIndex: 'createTime',
            render: (t) => new Date(t).toLocaleString()
        },
        {
            title: t('knowledgeBase.columns.actions'),
            key: 'action',
            render: (_, record) => (
                <Space>
                    <Button 
                        type="text" 
                        icon={<EyeOutlined />} 
                        onClick={() => handlePreview(record)}
                    >
                        {t('knowledgeBase.actions.preview')}
                    </Button>
                    <Button 
                        type="text" 
                        danger 
                        icon={<DeleteOutlined />} 
                        onClick={() => handleDelete(record)}
                    >
                        {t('knowledgeBase.actions.delete')}
                    </Button>
                </Space>
            )
        }
    ];

    return (
        <MainLayout>
             <div style={{ padding: '24px', maxWidth: '1200px', margin: '0 auto' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 24, alignItems: 'center' }}>
                    <Title level={3} style={{ margin: 0 }}>{t('knowledgeBase.title')}</Title>
                    <Upload
                        showUploadList={false}
                        customRequest={async ({ file, onSuccess, onError }) => {
                          try {
                            message.loading({ content: t('knowledgeBase.messages.uploading'), key: 'upload' });
                            const res = await uploadFile(file);
                            if(res && res.code === 200) {
                              const remoteFile = res.data;
                              message.loading({ content: t('knowledgeBase.messages.ingesting'), key: 'upload' });
                              await ingestFile(remoteFile.id, remoteFile.name);
                              message.success({ content: t('knowledgeBase.messages.uploadSuccess'), key: 'upload' });
                              onSuccess();
                              fetchData();
                            } else {
                              throw new Error(res.msg || t('knowledgeBase.messages.uploadFailed'));
                            }
                          } catch (err) {
                            console.error(err);
                            message.error({ content: t('knowledgeBase.messages.uploadFailed') + ': ' + err.message, key: 'upload' });
                            onError(err);
                          }
                        }}
                    >
                        <Button type="primary" icon={<CloudUploadOutlined />} size="large">{t('knowledgeBase.uploadButton')}</Button>
                    </Upload>
                </div>

                <div style={{ background: '#fff', padding: 24, borderRadius: 8, boxShadow: '0 1px 2px rgba(0,0,0,0.05)' }}>
                    <Table 
                        dataSource={data} 
                        columns={columns} 
                        rowKey="id" 
                        loading={loading}
                        pagination={{ pageSize: 10 }}
                    />
                </div>
             </div>
        </MainLayout>
    );
};

export default KnowledgeBasePage;
