import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import type { DevTokenResponse } from '../api/client';
import api from '../api/client';

export default function LoginPage() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleDevLogin = async () => {
    setLoading(true);
    setError('');
    try {
      const res = await api.get<DevTokenResponse>('/dev/token');
      localStorage.setItem('token', res.data.token);
      localStorage.setItem('email', res.data.email);
      localStorage.setItem('role', res.data.role);
      navigate('/');
    } catch {
      setError('ログインに失敗しました。バックエンドが起動しているか確認してください。');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <h1>感動創出レシピツール</h1>
        <p className="login-subtitle">料理の可能性を拡張する感動創出のプラットフォーム</p>
        <button
          onClick={handleDevLogin}
          disabled={loading}
          className="btn btn-primary btn-large"
        >
          {loading ? 'ログイン中...' : '開発モードでログイン'}
        </button>
        {error && <p className="error-message">{error}</p>}
        <p className="login-note">※ 開発環境用ログイン（PRODUCER権限）</p>
      </div>
    </div>
  );
}
