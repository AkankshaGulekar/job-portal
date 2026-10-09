import { useState } from 'react';
import Auth from './components/Auth';
import Jobs from './components/Jobs';
import MyApplications from './components/MyApplications';
import Recruiter from './components/Recruiter';

export default function App() {
  const [user, setUser] = useState(() => JSON.parse(localStorage.getItem('user') || 'null'));
  const [tab, setTab] = useState('jobs');

  const onAuth = (data) => {
    localStorage.setItem('token', data.token);
    localStorage.setItem('user', JSON.stringify(data));
    setUser(data);
    setTab(data.role === 'RECRUITER' ? 'dashboard' : 'jobs');
  };
  const logout = () => {
    localStorage.clear();
    setUser(null);
  };

  return (
    <>
      <header>
        <strong>Job Portal</strong>
        {user && (
          <nav>
            <span>{user.name} ({user.role})</span>
            <button className="secondary" onClick={() => setTab('jobs')}>Jobs</button>
            {user.role === 'CANDIDATE' && <button className="secondary" onClick={() => setTab('applications')}>My Applications</button>}
            {user.role === 'RECRUITER' && <button className="secondary" onClick={() => setTab('dashboard')}>Dashboard</button>}
            <button className="danger" onClick={logout}>Logout</button>
          </nav>
        )}
      </header>
      <main>
        {!user && <Auth onAuth={onAuth} />}
        {user && tab === 'jobs' && <Jobs user={user} />}
        {user && tab === 'applications' && <MyApplications />}
        {user && tab === 'dashboard' && <Recruiter />}
      </main>
    </>
  );
}
