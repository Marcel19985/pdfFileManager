import axios from "axios";

export const api = axios.create({
    baseURL: "/api", // dank Vite-Proxy geht das lokal an http://localhost:8080
});

// Types optional – anpassen an euer DTO
export type DocumentDto = {
    id: string;
    title: string;
    description?: string;
    status?: string;
    createdAt?: string;
};

export async function listDocuments(): Promise<DocumentDto[]> {
    const { data } = await api.get("/documents");
    return data;
}

export async function getDocument(id: string): Promise<DocumentDto> {
    const { data } = await api.get(`/documents/${id}`);
    return data;
}

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

export async function deleteDocument(id: string) {
    await api.delete(`/documents/${id}`);
}
