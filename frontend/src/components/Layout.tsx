import { Link, Outlet, useNavigate } from 'react-router-dom';

export default function Layout() {
  const navigate = useNavigate();
  const token = localStorage.getItem('token');
  const role = localStorage.getItem('role');
  const email = localStorage.getItem('email');

  const handleLogout = () => {
    localStorage.clear();
    navigate('/login');
  };

  if (!token) {
    navigate('/login');
    return null;
  }

  return (
    <div className="app">
      <header className="header">
        <div className="header-inner">
          <Link to="/" className="logo">感動レシピ</Link>
          <nav className="nav">
            <Link to="/">レシピ一覧</Link>
            <Link to="/recipes/new">新規作成</Link>
          </nav>
          <div className="user-info">
            <span className="user-badge">{role}</span>
            <span className="user-email">{email}</span>
            <button onClick={handleLogout} className="btn-logout">ログアウト</button>
          </div>
        </div>
      </header>
      <main className="main">
        <Outlet />
      </main>
    </div>
  );
}
