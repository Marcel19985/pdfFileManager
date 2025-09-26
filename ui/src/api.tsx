import axios from "axios";

export const api = axios.create({
    baseURL: "/api", // dank Vite-Proxy geht das lokal an http://localhost:8080
});

// Types optional – anpassen an euer DTO
export type DocumentDto = {
    id: string;
    title: string;
    description?: string;
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
