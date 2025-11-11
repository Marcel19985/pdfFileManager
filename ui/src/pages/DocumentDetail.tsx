import {useEffect, useRef, useState} from "react";
import { useParams, Link, useNavigate } from "react-router-dom";
import { getDocument, deleteDocument, type DocumentDto } from "../api";

export default function DocumentDetail() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const [doc, setDoc] = useState<DocumentDto | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const pollCountRef = useRef(0);
    const [polling, setPolling] = useState(false);

    useEffect(() => {
        if (!id) return;
        setLoading(true);
        getDocument(id)
            .then((d) => {
                setDoc(d);
                // Start-Polling, falls noch keine Summary vorhanden
                if (!d.summary || d.summary.length === 0) {
                    pollCountRef.current = 0;
                    setPolling(true);
                }
            })
            .catch((e) => setError(e?.message ?? "Failed to load"))
            .finally(() => setLoading(false));
    }, [id]);

    // Auto-Polling bis summary da ist oder Timeout erreicht
    useEffect(() => {
        if (!polling || !id) return;
        const interval = setInterval(async () => {
            try {
                pollCountRef.current += 1;
                const fresh = await getDocument(id);
                setDoc(fresh);
                const hasSummary = !!(fresh.summary && fresh.summary.length > 0);
                const tooLong = pollCountRef.current >= 30; // ~90s bei 3s-Intervallen
                if (hasSummary || tooLong) setPolling(false);
            } catch {
                // bei Fehlern Polling ebenfalls stoppen, sonst Loop
                setPolling(false);
            }
        }, 3000);
        return () => clearInterval(interval);
    }, [polling, id]);

    async function handleDelete() {
        if (!id) return;
        if (!window.confirm("Delete this document?")) return;
        try {
            await deleteDocument(id);
            navigate("/");
        } catch (e: any) {
            alert("Delete failed: " + e?.message);
        }
    }

    if (loading) return <p>Loading…</p>;
    if (error) return <p style={{ color: "red" }}>{error}</p>;
    if (!doc) return <p>Not found.</p>;

    const fileUrl = `/api/documents/${doc.id}/file`;

    return (
        <main style={{maxWidth: 800, margin: "2rem auto", padding: "0 1rem"}}>
            <p><Link to="/">← Back</Link></p>
            <h1>{doc.title}</h1>
            {doc.description && <p>{doc.description}</p>}
            {doc.createdAt && (
                <p><small>Created: {new Date(doc.createdAt).toLocaleString()}</small></p>
            )}

            <button onClick={handleDelete} style={{marginTop: "1rem", color: "red"}}>
                Delete
            </button>

            {/* --- AI Summary Bereich --- */}
            <section style={{ marginTop: "2rem" }}>
                <h2>Zusammenfassung</h2>

                {doc.summary?.trim() ? (
                    <p style={{ whiteSpace: "pre-wrap", lineHeight: 1.5 }}>
                        {doc.summary}
                    </p>
                ) : (
                    <p style={{ color: "#777" }}>
                        {polling
                            ? "Wird verarbeitet… (aktualisiert automatisch)"
                            : "Zusammenfassung derzeit nicht verfügbar."}
                    </p>
                )}
            </section>


            {doc && (
                <section style={{marginTop: "2rem"}}>
                    <h2>Document Preview</h2>
                    <iframe
                        src={fileUrl}
                        width="100%"
                        height="400px"
                        style={{border: "1px solid #ccc", borderRadius: "8px"}}
                        title="Document preview"
                    ></iframe>

                    <p style={{marginTop: "1rem"}}>
                        <a
                            href={fileUrl}
                            download={`${doc.title}.pdf`}
                            target="_blank"
                            rel="noopener noreferrer"
                        >
                            Download PDF
                        </a>
                    </p>
                </section>
            )}


        </main>


    );

}

