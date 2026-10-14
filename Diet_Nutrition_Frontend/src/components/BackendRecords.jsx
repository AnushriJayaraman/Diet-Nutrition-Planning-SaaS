import "./BackendRecords.css";

function BackendRecords({ title, records, columns, loading, error, emptyMessage, reverseRecords = true }) {
  const orderedRecords = reverseRecords ? records.slice().reverse() : records;
  return (
    <section className="backend-records" aria-live="polite">
      <h2>{title}</h2>
      {loading && <p>Loading saved records...</p>}
      {error && <p className="backend-records-error">{error}</p>}
      {!loading && !error && records.length === 0 && <p>{emptyMessage}</p>}
      {!loading && !error && records.length > 0 && (
        <div className="backend-records-table-wrap">
          <table className="backend-records-table">
            <thead>
              <tr>
                {columns.map(([key, label]) => <th key={key}>{label}</th>)}
              </tr>
            </thead>
            <tbody>
              {orderedRecords.map((record, index) => (
                <tr key={record.id ?? index}>
                  {columns.map(([key]) => (
                    <td key={key}>{record[key] == null || record[key] === "" ? "—" : String(record[key])}</td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}

export default BackendRecords;
