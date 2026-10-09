import { useEffect, useState } from 'react';
import { api } from '../api';

const TYPES = ['FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP'];

export default function Jobs({ user }) {
  const [filters, setFilters] = useState({ keyword: '', location: '', jobType: '' });
  const [page, setPage] = useState(0);
  const [data, setData] = useState({ content: [], totalPages: 0 });
  const [applyingTo, setApplyingTo] = useState(null);
  const [file, setFile] = useState(null);
  const [msg, setMsg] = useState('');

  const load = async (p = page) => {
    const q = new URLSearchParams({ page: p, size: 5 });
    Object.entries(filters).forEach(([k, v]) => v && q.append(k, v));
    setData(await api(`/jobs?${q}`));
  };
  useEffect(() => { load(); }, [page]);

  const search = (e) => { e.preventDefault(); page === 0 ? load(0) : setPage(0); };

  const apply = async (jobId) => {
    setMsg('');
    try {
      const form = new FormData();
      form.append('resume', file);
      await api(`/jobs/${jobId}/apply`, { method: 'POST', form });
      setMsg('Application submitted!');
      setApplyingTo(null);
      setFile(null);
    } catch (err) {
      setMsg(err.message);
    }
  };

  return (
    <>
      <form className="card row" onSubmit={search}>
        <input placeholder="Keyword" value={filters.keyword} onChange={(e) => setFilters({ ...filters, keyword: e.target.value })} />
        <input placeholder="Location" value={filters.location} onChange={(e) => setFilters({ ...filters, location: e.target.value })} />
        <select value={filters.jobType} onChange={(e) => setFilters({ ...filters, jobType: e.target.value })}>
          <option value="">Any type</option>
          {TYPES.map((t) => <option key={t}>{t}</option>)}
        </select>
        <button>Search</button>
      </form>
      {msg && <p className="muted">{msg}</p>}
      {data.content.map((j) => (
        <div className="card" key={j.id}>
          <h3>{j.title} <span className="badge">{j.jobType}</span></h3>
          <p className="muted">{j.company} · {j.location} {j.salary ? `· ₹${j.salary}` : ''}</p>
          <p>{j.description}</p>
          {user.role === 'CANDIDATE' && (applyingTo === j.id ? (
            <div className="row">
              <input type="file" accept=".pdf,.doc,.docx" onChange={(e) => setFile(e.target.files[0])} />
              <button disabled={!file} onClick={() => apply(j.id)}>Submit</button>
              <button className="secondary" onClick={() => setApplyingTo(null)}>Cancel</button>
            </div>
          ) : <button onClick={() => setApplyingTo(j.id)}>Apply</button>)}
        </div>
      ))}
      <div className="row">
        <button className="secondary" disabled={page === 0} onClick={() => setPage(page - 1)}>Prev</button>
        <span>Page {page + 1} of {Math.max(data.totalPages, 1)}</span>
        <button className="secondary" disabled={page + 1 >= data.totalPages} onClick={() => setPage(page + 1)}>Next</button>
      </div>
    </>
  );
}
