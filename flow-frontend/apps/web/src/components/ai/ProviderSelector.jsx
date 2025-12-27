import React, { useState, useEffect } from 'react';
import { Select, Space, Typography, Spin } from 'antd';
import { RobotOutlined } from '@ant-design/icons';
import { listProviders } from '../../api/aiProvider';

const { Text } = Typography;

/**
 * AI 供应商和模型选择器组件
 * 
 * @param {Object} props
 * @param {number} props.providerId - 当前选中的供应商ID
 * @param {string} props.model - 当前选中的模型
 * @param {function} props.onProviderChange - 供应商变更回调 (providerId, providerData)
 * @param {function} props.onModelChange - 模型变更回调 (model)
 */
const ProviderSelector = ({ providerId, model, onProviderChange, onModelChange }) => {
    const [providers, setProviders] = useState([]);
    const [loading, setLoading] = useState(false);
    const [models, setModels] = useState([]);

    useEffect(() => {
        fetchProviders();
    }, []);

    // 当供应商变化时，更新可用模型列表
    useEffect(() => {
        if (providerId && providers.length > 0) {
            const provider = providers.find(p => p.id === providerId);
            if (provider && provider.models) {
                const modelList = provider.models.split(',').map(m => m.trim()).filter(Boolean);
                setModels(modelList);
                // 如果当前模型不在列表中，选择第一个
                if (modelList.length > 0 && (!model || !modelList.includes(model))) {
                    onModelChange?.(modelList[0]);
                }
            } else {
                setModels([]);
            }
        }
    }, [providerId, providers]);

    const fetchProviders = async () => {
        try {
            setLoading(true);
            const res = await listProviders();
            if (res?.code === 200) {
                const enabledProviders = (res.data || []).filter(p => p.enabled);
                setProviders(enabledProviders);
                
                // 如果还没选择供应商，自动选择第一个
                if (enabledProviders.length > 0 && !providerId) {
                    onProviderChange?.(enabledProviders[0].id, enabledProviders[0]);
                }
            }
        } catch (err) {
            console.error('Failed to load providers:', err);
        } finally {
            setLoading(false);
        }
    };

    const handleProviderChange = (value) => {
        const provider = providers.find(p => p.id === value);
        onProviderChange?.(value, provider);
    };

    if (loading) {
        return <Spin size="small" />;
    }

    if (providers.length === 0) {
        return (
            <Text type="secondary" style={{ fontSize: 12 }}>
                <RobotOutlined /> 请先在设置中添加 AI 供应商
            </Text>
        );
    }

    return (
        <Space size={8}>
            <RobotOutlined style={{ color: '#1677ff' }} />
            <Select
                value={providerId}
                onChange={handleProviderChange}
                style={{ minWidth: 140 }}
                size="small"
                variant="borderless"
                options={providers.map(p => ({
                    value: p.id,
                    label: p.name,
                }))}
            />
            {models.length > 0 && (
                <>
                    <Text type="secondary">/</Text>
                    <Select
                        value={model}
                        onChange={onModelChange}
                        style={{ minWidth: 160 }}
                        size="small"
                        variant="borderless"
                        options={models.map(m => ({
                            value: m,
                            label: m,
                        }))}
                    />
                </>
            )}
        </Space>
    );
};

export default ProviderSelector;
