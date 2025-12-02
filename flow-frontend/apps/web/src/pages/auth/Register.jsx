import React from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import GlassPanel from '../../components/ui/GlassPanel';
import Button from '../../components/ui/Button';
import './Auth.css';

const Register = () => {
  const { register, handleSubmit, formState: { errors } } = useForm();
  const { register: registerUser } = useAuth();
  const navigate = useNavigate();

  const onSubmit = async (data) => {
    try {
      await registerUser(data.username, data.password);
      alert('Registration successful! Please login.');
      navigate('/login');
    } catch (error) {
      alert('Registration failed: ' + (error.response?.data?.message || error.message));
    }
  };

  return (
    <div className="auth-container">
      <GlassPanel className="auth-card">
        <h2 className="auth-title">Join Flow</h2>
        <p className="auth-subtitle">Create your account today</p>
        
        <form onSubmit={handleSubmit(onSubmit)} className="auth-form">
          <div className="form-group">
            <label>Username</label>
            <input 
              {...register('username', { required: true })} 
              className="glass-input"
              placeholder="Choose a username"
            />
            {errors.username && <span className="error-text">Username is required</span>}
          </div>
          
          <div className="form-group">
            <label>Password</label>
            <input 
              type="password"
              {...register('password', { required: true })} 
              className="glass-input"
              placeholder="Create a password"
            />
            {errors.password && <span className="error-text">Password is required</span>}
          </div>
          
          <Button type="submit" variant="primary" className="auth-btn">
            Sign Up
          </Button>
        </form>
        
        <p className="auth-footer">
          Already have an account? <Link to="/login" className="auth-link">Sign in</Link>
        </p>
      </GlassPanel>
    </div>
  );
};

export default Register;
