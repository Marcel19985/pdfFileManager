import axios from "axios";

export const api = axios.create({
    baseURL: "/api",
});

// Types
export type DocumentDto = {
    id: string;
    title: string;
    description?: string;
    createdAt?: string;
    summary?: string | null;
};

// Alle Dokumente holen
export async function listDocuments(): Promise<DocumentDto[]> {
    const { data } = await api.get("/documents");
    return data;
}

// Ein Dokument holen
export async function getDocument(id: string): Promise<DocumentDto> {
    const { data } = await api.get(`/documents/${id}`);
    return data;
}

// Hochladen
export async function uploadDocument(file: File, title: string, description?: string) {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("title", title);
    if (description) formData.append("description", description);

    const { data } = await api.post("/documents", formData, {
        headers: { "Content-Type": "multipart/form-data" },
    });
    return data as DocumentDto;
}

// Löschen
export async function deleteDocument(id: string) {
    await api.delete(`/documents/${id}`);
}

// Update
export async function updateDocument(id: string, title: string, description?: string) {
    const form = new FormData();
    form.append("title", title);
    if (description) form.append("description", description);

    const { data } = await api.put(`/documents/${id}`, form, {
        headers: { "Content-Type": "multipart/form-data" },
    });
    return data as DocumentDto;
}

// 🔍 Suche
export async function searchDocuments(query: string): Promise<DocumentDto[]> {
    const res = await fetch(`/api/documents/search?q=${encodeURIComponent(query)}`);
    if (!res.ok) throw new Error("Fehler bei der Suche");
    return res.json();
}
