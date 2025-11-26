import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
    listDocuments,
    uploadDocument,
    updateDocument,
    searchDocuments,
    type DocumentDto,
} from "../api";
import "../Dashboard.css";

// Kategorie Icons definieren
function getCategoryIcon(name?: string) {
    if (!name) return "🏷️"; // Standard-Icon, falls keine Kategorie
    switch(name.toLowerCase()) {
        case "geschichte": return "📜";
        case "rechnung": return "🧾";
        case "brief": return "✉️";
        case "schule": return "📚";
        case "wissenschaft": return "🔬";
        case "vertrag": return "📄";
        case "medizin": return "💉";
        case "technik": return "⚙️";
        case "sonstiges": return "📦";
        default: return "🏷️";
    }
}

export default function Dashboard() {
    const [docs, setDocs] = useState<DocumentDto[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    // Suche
    const [search, setSearch] = useState("");
    const [isSearching, setIsSearching] = useState(false);

    // Formular-States
    const [file, setFile] = useState<File | null>(null);
    const [title, setTitle] = useState("");
    const [description, setDescription] = useState("");

    // Edit-Modus
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

    useEffect(() => {
        const interval = setInterval(async () => {
            if (docs.some(d => !d.categoryName)) {
                const updated = await listDocuments();
                setDocs(updated);
            }
        }, 3000);

        return () => clearInterval(interval);
    }, [docs]);

    async function performSearch() {
        if (!search.trim()) return;
        setLoading(true);
        setIsSearching(true);

        try {
            const results = await searchDocuments(search);
            setDocs(results);
        } catch (e) {
            alert("Suche fehlgeschlagen");
        } finally {
            setLoading(false);
        }
    }

    function cancelSearch() {
        setSearch("");
        setIsSearching(false);
        loadDocs();
    }

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();

        try {
            if (editing) {
                if (!title.trim()) {
                    alert("Titel ist erforderlich");
                    return;
                }
                await updateDocument(editing.id, title, description);
            } else {
                if (!file || !title.trim()) {
                    alert("Datei und Titel sind erforderlich");
                    return;
                }
                await uploadDocument(file, title, description);
                setFile(null);
            }

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
        setFile(null);
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

                {/* Linke Spalte */}
                <div className="dashboard-column dashboard-left">
                    <h2>Dokumente</h2>

                    <div className="search-container">
                        <input
                            className="search-input"
                            type="text"
                            placeholder="🔍 Suche in Dokumenten..."
                            value={search}
                            onChange={(e) => setSearch(e.target.value)}
                        />
                        <button className="search-button" onClick={performSearch}>🔍</button>
                        {isSearching && (
                            <button className="cancel-button" onClick={cancelSearch}>❌</button>
                        )}
                    </div>

                    {docs.length === 0 ? (
                        <p className="muted">Keine Dokumente vorhanden.</p>
                    ) : (
                        <ul className="document-list">
                            {docs.map((d) => (
                                <li key={d.id} className="document-item">

                                    {/* Kategorie Icons */}
                                    {d.categoryName && (
                                        <span
                                            className="document-category-icon"
                                            data-category={d.categoryName}
                                            title={d.categoryName} // Tooltip
                                        >
                                            {getCategoryIcon(d.categoryName)}
                                        </span>
                                    )}

                                    <Link to={`/documents/${d.id}`} className="document-title">
                                        {d.title}
                                    </Link>
                                    {d.description && (
                                        <p className="document-desc">{d.description}</p>
                                    )}

                                    <div className="document-actions">
                                        <button type="button" onClick={() => startEdit(d)}>
                                            ✏️ Bearbeiten
                                        </button>
                                    </div>
                                </li>
                            ))}
                        </ul>
                    )}
                </div>

                {/* Rechte Spalte */}
                <div className="dashboard-column dashboard-right">
                    <h2>{editing ? "Dokument bearbeiten" : "Upload"}</h2>

                    <form className="upload-form" onSubmit={handleSubmit}>
                        {editing ? (
                            <p>Sie bearbeiten derzeit ein bestehendes Dokument.</p>
                        ) : (
                            <>
                                <p>Bitte wählen Sie ein Dokument aus.</p>
                                <label className="upload-label">
                                    Datei auswählen:
                                    <input
                                        type="file"
                                        onChange={(e) => setFile(e.target.files?.[0] ?? null)}
                                        required={!editing}
                                    />
                                </label>
                            </>
                        )}

                        <label className="upload-label">
                            Titel:
                            <input
                                type="text"
                                value={title}
                                onChange={(e) => setTitle(e.target.value)}
                                required
                            />
                        </label>

                        <label className="upload-label">
                            Beschreibung:
                            <textarea
                                value={description}
                                onChange={(e) => setDescription(e.target.value)}
                            />
                        </label>

                        <div className="form-actions">
                            <button type="submit" className="upload-button">
                                {editing ? "💾 Speichern" : "📤 Hochladen"}
                            </button>
                            {editing && (
                                <button
                                    type="button"
                                    className="secondary-button"
                                    onClick={cancelEdit}
                                >
                                    ❌ Abbrechen
                                </button>
                            )}
                        </div>
                    </form>
                </div>
            </main>
        </div>
    );
}
