import React from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import GlassPanel from '../../components/ui/GlassPanel';
import Button from '../../components/ui/Button';
import './Auth.css';

const Login = () => {
  const { register, handleSubmit, formState: { errors } } = useForm();
  const { login } = useAuth();
  const navigate = useNavigate();

  const onSubmit = async (data) => {
    try {
      await login(data.username, data.password);
      navigate('/');
    } catch (error) {
      alert('Login failed: ' + (error.response?.data?.message || error.message));
    }
  };

  return (
    <div className="auth-container">
      <GlassPanel className="auth-card">
        <h2 className="auth-title">Welcome Back</h2>
        <p className="auth-subtitle">Sign in to continue your flow</p>
        
        <form onSubmit={handleSubmit(onSubmit)} className="auth-form">
          <div className="form-group">
            <label>Username</label>
            <input 
              {...register('username', { required: true })} 
              className="glass-input"
              placeholder="Enter your username"
            />
            {errors.username && <span className="error-text">Username is required</span>}
          </div>
          
          <div className="form-group">
            <label>Password</label>
            <input 
              type="password"
              {...register('password', { required: true })} 
              className="glass-input"
              placeholder="Enter your password"
            />
            {errors.password && <span className="error-text">Password is required</span>}
          </div>
          
          <Button type="submit" variant="primary" className="auth-btn">
            Sign In
          </Button>
        </form>
        
        <p className="auth-footer">
          Don't have an account? <Link to="/register" className="auth-link">Sign up</Link>
        </p>
      </GlassPanel>
    </div>
  );
};

export default Login;
