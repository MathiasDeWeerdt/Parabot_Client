import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JOptionPane;

import sign.signlink;

public class CacheDownloader {

	public client client;
	public final int BUFFER = 1024;
	public static final int VERSION = 2;
	public String cacheLink = "https://dl.dropboxusercontent.com/u/9637944/cache.zip";
	public String fileToExtract = getCacheDir() + getArchivedName();

	public CacheDownloader(client client) {
		this.client = client;
	}

	public void drawLoadingText(String text) {
		client.drawLoadingText(35, text);
	}

	public void drawLoadingText(int amount, String text) {
		client.drawLoadingText(amount, text);
	}

	public String getCacheDir() {
		return signlink.findcachedir();
	}

	public String getCacheLink() {
		return cacheLink;
	}

	public int getCacheVersion() {
		return VERSION;
	}

	private void alert(String msg) {
		alert("Message", msg, false);
	}

	private void alert(String title, String msg, boolean error) {
		JOptionPane
				.showMessageDialog(null, msg, title,
						(error ? JOptionPane.ERROR_MESSAGE
								: JOptionPane.PLAIN_MESSAGE));
	}

	public void DoesNotHaveCacheMessage() {
		alert("You do not have the cache. Cache will now be downloaded, please do not interrupt the download!");
		alert("If Client goes white, it's unpacking cache - let it load!!");
	}

	public CacheDownloader downloadCache() {
		try {
			File location = new File(getCacheDir());
			File version = new File(getCacheDir() + "/cacheUpdate"
					+ getCacheVersion() + ".dat");
			if (!location.exists()) {
				// delete(getCacheDir());
				DoesNotHaveCacheMessage();
				downloadFile(getCacheLink(), getArchivedName());
				unZip();
				BufferedWriter versionFile = new BufferedWriter(new FileWriter(
						getCacheDir() + "/cacheUpdate" + getCacheVersion()
								+ ".dat"));
				versionFile.close();
				deleteDownloadedZipFile(getArchivedName());
			} else {
				if (!version.exists()) {
					DoesNotHaveCacheMessage();
					// delete(getCacheDir());
					downloadFile(getCacheLink(), getArchivedName());
					unZip();
					BufferedWriter versionFile = new BufferedWriter(
							new FileWriter(getCacheDir() + "/cacheUpdate"
									+ getCacheVersion() + ".dat"));
					versionFile.close();
					deleteDownloadedZipFile(getArchivedName());
				} else {
					return null;
				}
			}
		} catch (Exception e) {
		}
		return null;
	}

	public void downloadFile(String adress, String localFileName) {
		OutputStream out = null;
		URLConnection conn;
		InputStream in = null;
		try {
			URL url = new URL(adress);
			out = new BufferedOutputStream(new FileOutputStream(getCacheDir()
					+ "/" + localFileName));
			conn = url.openConnection();
			in = conn.getInputStream();
			byte[] data = new byte[BUFFER];
			int numRead;
			long numWritten = 0;
			int length = conn.getContentLength();
			while ((numRead = in.read(data)) != -1) {
				out.write(data, 0, numRead);
				numWritten += numRead;
				int percentage = (int) (((double) numWritten / (double) length) * 100D);
				drawLoadingText(percentage, "Downloading Cache " + percentage
						+ "%");
			}
			drawLoadingText("Finished downloading " + getArchivedName() + "!");
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (IOException ioe) {
			}
		}
	}

	public String getArchivedName() {
		int lastSlashIndex = getCacheLink().lastIndexOf('/');
		if (lastSlashIndex >= 0 && lastSlashIndex < getCacheLink().length() - 1) {
			return getCacheLink().substring(lastSlashIndex + 1);
		} else {
		}
		return "";
	}

	public void deleteDownloadedZipFile(String fileName) {
		File f = new File(getCacheDir() + fileName);
		if (!f.exists())
			throw new IllegalArgumentException(
					"Delete: no such file or directory: " + fileName);
		if (!f.canWrite())
			throw new IllegalArgumentException("Delete: write protected: "
					+ fileName);
		if (f.isDirectory()) {
			String[] files = f.list();
			if (files.length > 0)
				throw new IllegalArgumentException(
						"Delete: directory not empty: " + fileName);
		}
		boolean success = f.delete();
		if (!success)
			throw new IllegalArgumentException("Delete: deletion failed");
	}

	public static boolean delete(String path) {
		if (path == null || path.trim().length() == 0)
			return false;
		return delete(new File(path));
	}

	public static boolean delete(File fileOrDirectory) {
		if (fileOrDirectory == null)
			return false;
		if (!fileOrDirectory.exists())
			return false;
		if (fileOrDirectory.isDirectory()) {
			for (File childFile : fileOrDirectory.listFiles()) {
				delete(childFile);
			}
		}
		return fileOrDirectory.delete();
	}

	public void unZip() {
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(
					fileToExtract));
			ZipInputStream zin = new ZipInputStream(in);
			ZipEntry e;
			while ((e = zin.getNextEntry()) != null) {
				if (e.isDirectory()) {
					(new File(getCacheDir() + e.getName())).mkdir();
				} else {
					if (e.getName().equals(fileToExtract)) {
						unzip(zin, fileToExtract);
						break;
					}
					unzip(zin, getCacheDir() + e.getName());
				}
			}
			zin.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void unzip(ZipInputStream zin, String s) throws IOException {
		FileOutputStream out = new FileOutputStream(s);
		byte[] b = new byte[BUFFER];
		int len = 0;
		while ((len = zin.read(b)) != -1) {
			out.write(b, 0, len);
		}
		out.close();
	}
}