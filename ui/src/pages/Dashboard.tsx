import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { listDocuments, uploadDocument, type DocumentDto } from "../api";
import "../Dashboard.css";

export default function Dashboard() {
    // --- States ---
    const [docs, setDocs] = useState<DocumentDto[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const [file, setFile] = useState<File | null>(null);
    const [title, setTitle] = useState("");
    const [description, setDescription] = useState("");

    // --- Dokumente laden ---
    async function loadDocs() {
        setLoading(true);
        try {
            const data = await listDocuments();
            setDocs(data);
        } catch (e: any) {
            setError(e?.message ?? "Fehler beim Laden der Dokumente");
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        loadDocs();
    }, []);

    // --- Upload ---
    async function handleUpload(e: React.FormEvent) {
        e.preventDefault();
        if (!file || !title) {
            alert("Datei und Titel sind erforderlich");
            return;
        }
        try {
            await uploadDocument(file, title, description);
            setFile(null);
            setTitle("");
            setDescription("");
            await loadDocs();
        } catch (e: any) {
            alert("Upload fehlgeschlagen: " + e?.message);
        }
    }

    if (loading) return <p className="loading">Lädt...</p>;
    if (error) return <p className="error">{error}</p>;

    return (
        <div className="dashboard-page">
            {/* Header */}
            <header className="dashboard-header">
                <h1>PDF MANAGER</h1>
            </header>

            {/* Zwei Spalten */}
            <main className="dashboard-content">
                {/* Linke Spalte: Dokumente */}
                <div className="dashboard-column dashboard-left">
                    <h2>Dokumente</h2>
                    {docs.length === 0 ? (
                        <p className="muted">Keine Dokumente vorhanden.</p>
                    ) : (
                        <ul className="document-list">
                            {docs.map((d) => (
                                <li key={d.id} className="document-item">
                                    <Link to={`/documents/${d.id}`} className="document-title">
                                        {d.title}
                                    </Link>
                                    {d.description && <p className="document-desc">{d.description}</p>}
                                </li>
                            ))}
                        </ul>
                    )}
                </div>

                {/* Rechte Spalte: Upload */}
                <div className="dashboard-column dashboard-right">
                    <h2>Upload</h2>
                    <form className="upload-form" onSubmit={handleUpload}>
                        <p>Bitte wählen Sie ein Dokument (derzeit PDF) aus, welches Sie hochladen möchten: ------------------------------- </p>

                            <label className="upload-label">
                                Datei auswählen:
                                <input
                                    type="file"
                                    onChange={(e) => setFile(e.target.files?.[0] ?? null)}
                                    required
                                />
                            </label>

                            <label className="upload-label">
                                Titel:
                                <input
                                    type="text"
                                    placeholder="Titel des Dokuments"
                                    value={title}
                                    onChange={(e) => setTitle(e.target.value)}
                                    required
                                />
                            </label>

                            <label className="upload-label">
                                Beschreibung (optional):
                                <textarea
                                    placeholder="Kurze Beschreibung"
                                    value={description}
                                    onChange={(e) => setDescription(e.target.value)}
                                />
                            </label>

                            <button type="submit" className="upload-button">
                                📤 Hochladen
                            </button>
                    </form>
                </div>
            </main>
        </div>
    );
}
