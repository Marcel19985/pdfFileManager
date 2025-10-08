import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { listDocuments, uploadDocument, updateDocument, type DocumentDto } from "../api";
import "../Dashboard.css";

export default function Dashboard() {
    const [docs, setDocs] = useState<DocumentDto[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    // Formular-States
    const [file, setFile] = useState<File | null>(null);
    const [title, setTitle] = useState("");
    const [description, setDescription] = useState("");

    // Edit-Modus: null = Create, sonst Dokument bearbeiten
    const [editing, setEditing] = useState<DocumentDto | null>(null);

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

    // Submit entscheidet je nach Modus
    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();

        try {
            if (editing) {
                // EDIT: nur Titel/Beschreibung, KEIN File (laut Backend)
                if (!title.trim()) {
                    alert("Titel ist erforderlich");
                    return;
                }
                await updateDocument(editing.id, title, description);
            } else {
                // CREATE
                if (!file || !title.trim()) {
                    alert("Datei und Titel sind erforderlich");
                    return;
                }
                await uploadDocument(file, title, description);
                setFile(null);
            }

            // Reset + Refresh
            setTitle("");
            setDescription("");
            setEditing(null);
            await loadDocs();
        } catch (e: any) {
            alert((editing ? "Update" : "Upload") + " fehlgeschlagen: " + (e?.message ?? ""));
        }
    }

    function startEdit(d: DocumentDto) {
        setEditing(d);
        setTitle(d.title ?? "");
        setDescription(d.description ?? "");
        setFile(null); // beim Edit nicht benötigt
    }

    function cancelEdit() {
        setEditing(null);
        setTitle("");
        setDescription("");
        setFile(null);
    }

    if (loading) return <p className="loading">Lädt...</p>;
    if (error) return <p className="error">{error}</p>;

    return (
        <div className="dashboard-page">
            <header className="dashboard-header">
                <h1>PDF MANAGER</h1>
            </header>

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

                                    <div className="document-actions">
                                        <button type="button" onClick={() => startEdit(d)}>
                                            ✏️ Bearbeiten
                                        </button>
                                        {/* Optional: Delete-Button, falls du willst */}
                                    </div>
                                </li>
                            ))}
                        </ul>
                    )}
                </div>

                {/* Rechte Spalte: Create/Edit */}
                <div className="dashboard-column dashboard-right">
                    <h2>{editing ? "Dokument bearbeiten" : "Upload"}</h2>

                    <form className="upload-form" onSubmit={handleSubmit}>
                        {editing ? (
                            <p>Sie bearbeiten derzeit ein bestehendes Dokument. Die Datei selbst kann nicht geändert werden, nur Titel und Beschreibung.</p>
                        ) : (
                            <>
                                <p>Bitte wählen Sie ein Dokument (derzeit PDF) aus, welches Sie hochladen möchten, geben sie hierfür auch Titel und Beschreibung ein.</p>
                                <label className="upload-label">
                                    Datei auswählen:
                                    <input
                                        type="file"
                                        onChange={(e) => setFile(e.target.files?.[0] ?? null)}
                                        required={!editing} // im Create erforderlich
                                    />
                                </label>
                            </>
                        )}

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

                        <div className="form-actions">
                            <button type="submit" className="upload-button">
                                {editing ? "💾 Speichern" : "📤 Hochladen"}
                            </button>
                            {editing && (
                                <button type="button" className="secondary-button" onClick={cancelEdit}>
                                    ❌ Abbrechen
                                </button>
                            )}
                        </div>
                    </form>

                    {editing && (
                        <p className="muted">
                            Bearbeite: <code>{editing.title}</code> (ID: {editing.id})
                        </p>
                    )}
                </div>
            </main>
        </div>
    );
}
