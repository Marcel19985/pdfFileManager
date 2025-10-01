import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { listDocuments, uploadDocument, type DocumentDto } from "../api";
import "../Dashboard.css";

export default function Dashboard() {
    const [docs, setDocs] = useState<DocumentDto[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const [file, setFile] = useState<File | null>(null);
    const [title, setTitle] = useState("");
    const [description, setDescription] = useState("");

    async function loadDocs() {
        setLoading(true);
        try {
            const data = await listDocuments();
            setDocs(data);
        } catch (e: any) {
            setError(e?.message ?? "Failed to load");
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        loadDocs();
    }, []);

    async function handleUpload(e: React.FormEvent) {
        e.preventDefault();
        if (!file || !title) {
            alert("File and title required");
            return;
        }
        try {
            await uploadDocument(file, title, description);
            setFile(null);
            setTitle("");
            setDescription("");
            await loadDocs();
        } catch (e: any) {
            alert("Upload failed: " + e?.message);
        }
    }

    if (loading) return <p>Loading…</p>;
    if (error) return <p className="error">{error}</p>;

    return (

        <main>
            <div className="dashboard-container">
                <h1>PDF Manager</h1>
                <div className="dashboard-flex">
                    {/* Links: Dokumentliste */}
                    <div className="dashboard-docs">
                        <h2>Documents</h2>
                        {docs.length === 0 ? (
                            <p>No documents yet.</p>
                        ) : (
                            <ul>
                                {docs.map(d => (
                                    <li key={d.id}>
                                        <Link to={`/documents/${d.id}`}>{d.title}</Link>
                                        {d.description && <p className="desc">{d.description}</p>}
                                    </li>
                                ))}
                            </ul>
                        )}
                    </div>

                    {/* Vertikale Mittellinie */}
                    <div className="dashboard-divider"></div>

                    {/* Rechts: Upload */}
                    <div className="dashboard-upload">
                        <h2>Upload</h2>
                        <form onSubmit={handleUpload}>
                            <input
                                type="file"
                                required={true}
                                onChange={e => setFile(e.target.files?.[0] ?? null)}
                            />
                            <input
                                type="text"
                                placeholder="Title"
                                value={title}
                                maxLength={50}
                                required={true}
                                pattern="[A-Za-z0-9 ]{1,200}"
                                onChange={e => setTitle(e.target.value)}
                            />
                            <textarea
                                placeholder="Description"
                                value={description}
                                maxLength={200}
                                onChange={e => setDescription(e.target.value)}
                            />

                            <button type="submit">Upload</button>
                        </form>
                    </div>
                </div>
            </div>
        </main>
    );
}
