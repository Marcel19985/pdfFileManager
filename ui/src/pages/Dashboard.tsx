import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { listDocuments, type DocumentDto } from "../api";

export default function Dashboard() {
    const [docs, setDocs] = useState<DocumentDto[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        listDocuments()
            .then(setDocs)
            .catch(e => setError(e?.message ?? "Failed to load"))
            .finally(() => setLoading(false));
    }, []);

    if (loading) return <p>Loading…</p>;
    if (error) return <p style={{ color: "red" }}>{error}</p>;

    return (
        <main style={{ maxWidth: 800, margin: "2rem auto", padding: "0 1rem" }}>
            <h1>Documents</h1>
            {docs.length === 0 ? (
                <p>No documents yet.</p>
            ) : (
                <ul>
                    {docs.map(d => (
                        <li key={d.id}>
                            <Link to={`/documents/${d.id}`}>{d.title}</Link>
                            {d.description ? <> — <small>{d.description}</small></> : null}
                        </li>
                    ))}
                </ul>
            )}
        </main>
    );
}
