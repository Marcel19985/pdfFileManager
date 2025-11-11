import axios from "axios";

export const api = axios.create({
    baseURL: "/api", // dank Vite-Proxy geht das lokal an http://localhost:8080
});

// Types – anpassen an DTO
export type DocumentDto = {
    id: string;
    title: string;
    description?: string;
    createdAt?: string;

    //optional, vom Backend kommend
    summary?: string | null;
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

export async function updateDocument(id: string, title: string, description?: string) {
    const form = new FormData();
    form.append("title", title);
    if (description) form.append("description", description);

    const { data } = await api.put(`/documents/${id}`, form, {
        headers: { "Content-Type": "multipart/form-data" },
    });
    return data as DocumentDto;

}
