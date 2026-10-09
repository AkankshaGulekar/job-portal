import { useEffect, useState } from 'react';
import { api } from '../api';

export default function MyApplications() {
  const [apps, setApps] = useState([]);

  useEffect(() => {
    api('/applications/mine?size=50').then((d) => setApps(d.content));
  }, []);

  return (
    <>
      <h2>My Applications</h2>
      {apps.length === 0 && <p className="muted">No applications yet.</p>}
      {apps.map((a) => (
        <div className="card" key={a.id}>
          <strong>{a.jobTitle}</strong> at {a.company}{' '}
          <span className={`badge ${a.status}`}>{a.status}</span>
          <p className="muted">Applied on {new Date(a.appliedAt).toLocaleDateString()}</p>
        </div>
      ))}
    </>
  );
}
