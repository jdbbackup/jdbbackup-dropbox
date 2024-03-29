package com.fathzer.jdbbackup.destinations.dropbox;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.dropbox.core.DbxException;
import com.dropbox.core.oauth.DbxCredential;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.UploadBuilder;
import com.dropbox.core.v2.files.WriteMode;
import com.fathzer.jdbbackup.ProxyCompliant;
import com.fathzer.jdbbackup.destinations.dropbox.DropboxManager.DropboxDestination;
import com.fathzer.jdbbackup.utils.BasicExtensionBuilder;

class DropboxManagerTest {

	@Test
	void test() throws IOException, DbxException {
		final DropboxManager manager = new DropboxManager();
		assertTrue(manager instanceof ProxyCompliant);
		assertEquals("dropbox", manager.getScheme());
		try (MockedConstruction<Date> mock = mockConstruction(Date.class)) {
			DropboxDestination path = manager.validate("token/a/{d=MMyy}", BasicExtensionBuilder.INSTANCE);
			assertEquals("token", path.getToken());
			assertEquals("/a/0170.sql.gz", path.getPath());
		}
		assertThrows(IllegalArgumentException.class, () -> manager.validate("/a", BasicExtensionBuilder.INSTANCE));
		assertThrows(IllegalArgumentException.class, () -> manager.validate("token/", BasicExtensionBuilder.INSTANCE));
		assertThrows(IllegalArgumentException.class, () -> manager.validate("x", BasicExtensionBuilder.INSTANCE));
		
		FileMetadata data = mock(FileMetadata.class);
		UploadBuilder up = mock(UploadBuilder.class);
		when(up.uploadAndFinish(null,0)).thenReturn(data);
		final DbxUserFilesRequests files = mock(DbxUserFilesRequests.class);
		when(files.uploadBuilder("/file")).thenReturn(up);
		DropboxDestination dest = manager.validate("token/file", s->s);
		try (MockedConstruction<DbxClientV2> mock = mockConstruction(DbxClientV2.class, (client, context) -> {
			when(client.files()).thenReturn(files);
		})) {
			manager.send(null, 0, dest);
		}
		verify(up).withMode(WriteMode.OVERWRITE);
	}

	@Test
	void testCredentials() {
		DropboxManager manager = new DropboxManager();
		{
			final DbxCredential credential = manager.getCredential("token");
			assertAll(
				() -> assertEquals("token",credential.getAccessToken()),
				() -> assertFalse(credential.aboutToExpire()),
				() -> assertNull(credential.getRefreshToken())
				);
		}
		{
			final DbxCredential credential = manager.getCredential(DropboxBase.REFRESH_PREFIX+"token");
			assertAll(
				() -> assertTrue(credential.aboutToExpire()),
				() -> assertEquals("token",credential.getRefreshToken())
				);
		}
	}
}
