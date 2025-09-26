import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import { getDocument, type DocumentDto } from "../api";

export default function DocumentDetail() {
    const { id } = useParams<{ id: string }>();
    const [doc, setDoc] = useState<DocumentDto | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!id) return;
        getDocument(id)
            .then(setDoc)
            .catch(e => setError(e?.message ?? "Failed to load"))
            .finally(() => setLoading(false));
    }, [id]);

    if (loading) return <p>Loading…</p>;
    if (error) return <p style={{ color: "red" }}>{error}</p>;
    if (!doc) return <p>Not found.</p>;

    return (
        <main style={{ maxWidth: 800, margin: "2rem auto", padding: "0 1rem" }}>
            <p><Link to="/">← Back</Link></p>
            <h1>{doc.title}</h1>
            {doc.description && <p>{doc.description}</p>}
            {doc.createdAt && (
                <p><small>Created: {new Date(doc.createdAt).toLocaleString()}</small></p>
            )}
        </main>
    );
}
