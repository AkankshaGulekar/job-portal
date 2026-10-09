import { useEffect, useState } from 'react';
import { api, downloadResume } from '../api';

const TYPES = ['FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP'];
const empty = { title: '', description: '', company: '', location: '', salary: '', jobType: 'FULL_TIME' };

export default function Recruiter() {
  const [form, setForm] = useState(empty);
  const [jobs, setJobs] = useState([]);
  const [openJob, setOpenJob] = useState(null);
  const [applicants, setApplicants] = useState([]);
  const [error, setError] = useState('');
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const loadJobs = async () => setJobs((await api('/recruiter/jobs?size=50')).content);
  useEffect(() => { loadJobs(); }, []);

  const post = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await api('/jobs', { method: 'POST', body: { ...form, salary: form.salary ? Number(form.salary) : null } });
      setForm(empty);
      loadJobs();
    } catch (err) { setError(err.message); }
  };

  const remove = async (id) => { await api(`/jobs/${id}`, { method: 'DELETE' }); loadJobs(); };

  const toggle = async (id) => {
    if (openJob === id) return setOpenJob(null);
    setApplicants((await api(`/jobs/${id}/applications?size=50`)).content);
    setOpenJob(id);
  };

  const setStatus = async (appId, status) => {
    const updated = await api(`/applications/${appId}/status`, { method: 'PATCH', body: { status } });
    setApplicants(applicants.map((a) => (a.id === appId ? updated : a)));
  };

  return (
    <>
      <form className="card" onSubmit={post}>
        <h2>Post a job</h2>
        <input placeholder="Title" value={form.title} onChange={set('title')} required />
        <input placeholder="Company" value={form.company} onChange={set('company')} required />
        <input placeholder="Location" value={form.location} onChange={set('location')} required />
        <input type="number" placeholder="Salary (optional)" value={form.salary} onChange={set('salary')} />
        <select value={form.jobType} onChange={set('jobType')}>{TYPES.map((t) => <option key={t}>{t}</option>)}</select>
        <textarea rows="4" placeholder="Description" value={form.description} onChange={set('description')} required />
        {error && <p className="err">{error}</p>}
        <button>Post job</button>
      </form>

      <h2>My jobs</h2>
      {jobs.map((j) => (
        <div className="card" key={j.id}>
          <strong>{j.title}</strong> <span className="muted">{j.company} · {j.location}</span>
          <div className="row" style={{ marginTop: 8 }}>
            <button onClick={() => toggle(j.id)}>{openJob === j.id ? 'Hide' : 'View'} applicants</button>
            <button className="danger" onClick={() => remove(j.id)}>Delete</button>
          </div>
          {openJob === j.id && (applicants.length === 0 ? <p className="muted">No applicants yet.</p> :
            applicants.map((a) => (
              <div key={a.id} className="row" style={{ marginTop: 8 }}>
                <span>{a.candidateName} ({a.candidateEmail})</span>
                <span className={`badge ${a.status}`}>{a.status}</span>
                <button className="secondary" onClick={() => downloadResume(a.resumeUrl)}>Resume</button>
                <button onClick={() => setStatus(a.id, 'SHORTLISTED')}>Shortlist</button>
                <button className="danger" onClick={() => setStatus(a.id, 'REJECTED')}>Reject</button>
              </div>
            )))}
        </div>
      ))}
    </>
  );
}
