import { useState } from 'react';
import { api } from '../api';

export default function Auth({ onAuth }) {
  const [mode, setMode] = useState('login');
  const [form, setForm] = useState({ name: '', email: '', password: '', role: 'CANDIDATE' });
  const [error, setError] = useState('');
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      onAuth(await api(`/auth/${mode}`, { method: 'POST', body: form }));
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <form className="card" onSubmit={submit}>
      <h2>{mode === 'login' ? 'Login' : 'Create account'}</h2>
      {mode === 'register' && (
        <>
          <input placeholder="Name" value={form.name} onChange={set('name')} required />
          <select value={form.role} onChange={set('role')}>
            <option value="CANDIDATE">Candidate</option>
            <option value="RECRUITER">Recruiter</option>
          </select>
        </>
      )}
      <input type="email" placeholder="Email" value={form.email} onChange={set('email')} required />
      <input type="password" placeholder="Password" value={form.password} onChange={set('password')} required />
      {error && <p className="err">{error}</p>}
      <div className="row">
        <button type="submit">{mode === 'login' ? 'Login' : 'Register'}</button>
        <button type="button" className="secondary" onClick={() => setMode(mode === 'login' ? 'register' : 'login')}>
          {mode === 'login' ? 'Need an account?' : 'Have an account?'}
        </button>
      </div>
    </form>
  );
}
