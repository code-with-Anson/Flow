import React, { useState, useEffect } from 'react';
import { Form, Input, Select, Switch, Button, Space, message } from 'antd';
import { saveOrUpdateProvider } from '../../api/aiProvider';

const { Option } = Select;

const providerOptions = [
    { value: 'OPENAI', label: 'OpenAI / 兼容API' },
    { value: 'OLLAMA', label: 'Ollama (本地)' },
];

const ProviderForm = ({ initialValues, onSuccess, onCancel }) => {
    const [form] = Form.useForm();
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (initialValues) {
            form.setFieldsValue({
                ...initialValues,
                apiKey: initialValues.id ? '******' : '', // 编辑时显示掩码
            });
        } else {
            form.resetFields();
        }
    }, [initialValues, form]);

    const handleSubmit = async (values) => {
        try {
            setLoading(true);
            
            // 如果是编辑模式且 apiKey 是掩码，则保持原值
            const submitData = {
                ...values,
                id: initialValues?.id,
            };
            
            const res = await saveOrUpdateProvider(submitData);
            if (res?.code === 200) {
                message.success(initialValues?.id ? '更新成功' : '添加成功');
                onSuccess?.();
            } else {
                message.error(res?.message || '操作失败');
            }
        } catch (err) {
            console.error(err);
            message.error('操作失败');
        } finally {
            setLoading(false);
        }
    };

    return (
        <Form
            form={form}
            layout="vertical"
            onFinish={handleSubmit}
            initialValues={{
                provider: 'OPENAI',
                enabled: true,
                priority: 0,
            }}
        >
            <Form.Item
                name="name"
                label="配置名称"
                rules={[{ required: true, message: '请输入配置名称' }]}
            >
                <Input placeholder="例如: My DeepSeek" />
            </Form.Item>

            <Form.Item
                name="provider"
                label="供应商类型"
                rules={[{ required: true }]}
            >
                <Select placeholder="选择供应商类型">
                    {providerOptions.map(opt => (
                        <Option key={opt.value} value={opt.value}>{opt.label}</Option>
                    ))}
                </Select>
            </Form.Item>

            <Form.Item
                name="baseUrl"
                label="API 地址"
                rules={[{ required: true, message: '请输入 API 地址' }]}
            >
                <Input placeholder="例如: https://api.deepseek.com" />
            </Form.Item>

            <Form.Item
                name="apiKey"
                label="API Key"
                rules={[{ required: !initialValues?.id, message: '请输入 API Key' }]}
                extra={initialValues?.id ? '留空或输入 ****** 表示保持原有密钥' : null}
            >
                <Input.Password placeholder="sk-xxxx" />
            </Form.Item>

            <Form.Item
                name="models"
                label="可用模型"
                extra="多个模型用逗号分隔"
            >
                <Input placeholder="例如: deepseek-chat, deepseek-coder" />
            </Form.Item>

            <Form.Item
                name="priority"
                label="排序优先级"
            >
                <Input type="number" placeholder="数字越小越靠前" />
            </Form.Item>

            <Form.Item
                name="enabled"
                label="启用状态"
                valuePropName="checked"
            >
                <Switch />
            </Form.Item>

            <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
                <Space>
                    <Button onClick={onCancel}>取消</Button>
                    <Button type="primary" htmlType="submit" loading={loading}>
                        {initialValues?.id ? '更新' : '添加'}
                    </Button>
                </Space>
            </Form.Item>
        </Form>
    );
};

export default ProviderForm;
