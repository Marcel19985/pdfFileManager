package at.ac.fhtw.swen3.swen3teamm.service;

import io.minio.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.io.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class MinioServiceTest {

    @Mock MinioClient minioClient;
    MinioService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new MinioService(minioClient);
    }

    @Test
    void upload_createsBucketIfMissing_andPutsObject() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);

        byte[] bytes = "PDF".getBytes();
        try (InputStream in = new ByteArrayInputStream(bytes)) {
            service.upload("doc-1.pdf", in, bytes.length);
        }

        // bucketExists -> makeBucket -> putObject
        verify(minioClient).bucketExists(argThat(a -> a.bucket().equals("documents")));
        verify(minioClient).makeBucket(argThat(a -> a.bucket().equals("documents")));
        verify(minioClient).putObject(argThat(a ->
                a.bucket().equals("documents") &&
                        a.object().equals("doc-1.pdf")));
    }

    @Test
    void upload_doesNotCreateBucket_ifExists() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        byte[] bytes = "PDF".getBytes();
        try (InputStream in = new ByteArrayInputStream(bytes)) {
            service.upload("doc-2.pdf", in, bytes.length);
        }

        verify(minioClient, never()).makeBucket(any(MakeBucketArgs.class));
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    void upload_wrapsExceptionsIntoRuntime() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenThrow(new IOException("boom"));
        assertThatThrownBy(() ->
                service.upload("x.pdf", new ByteArrayInputStream(new byte[0]), 0)
        ).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error uploading to MinIO");
    }

    @Test
    void download_callsGetObject_andReturnsStream() throws Exception {
        byte[] data = "hello".getBytes();
        GetObjectResponse response = new GetObjectResponse(
                null,               // headers
                "documents",        // bucket
                "doc-3.pdf",        // object
                null,               // versionId
                new ByteArrayInputStream(data) // stream
        );
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(response);

        try (InputStream is = service.download("doc-3.pdf")) {
            byte[] bytes = is.readAllBytes();
            assertThat(new String(bytes)).isEqualTo("hello");
        }

        verify(minioClient).getObject(argThat(a ->
                a.bucket().equals("documents") && a.object().equals("doc-3.pdf")));
    }

    @Test
    void download_wrapsExceptionsIntoRuntime() throws Exception {
        when(minioClient.getObject(any(GetObjectArgs.class))).thenThrow(new RuntimeException("x"));
        assertThatThrownBy(() -> service.download("nope.pdf"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error fetching from MinIO");
    }

    @Test
    void delete_callsRemoveObject() throws Exception {
        service.delete("doc-4.pdf");
        verify(minioClient).removeObject(argThat(a ->
                a.bucket().equals("documents") &&
                        a.object().equals("doc-4.pdf")));
    }

    @Test
    void delete_wrapsExceptionsIntoRuntime() throws Exception {
        doThrow(new RuntimeException("fail")).when(minioClient).removeObject(any(RemoveObjectArgs.class));
        assertThatThrownBy(() -> service.delete("bad.pdf"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error deleting from MinIO");
    }
}