package com.globalsight.connector.git.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.DiffCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.DetachedHeadException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidConfigurationException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.NotSupportedException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.util.FS;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import com.globalsight.connector.git.vo.GitConnectorFile;
import com.globalsight.cxe.entity.gitconnector.GitConnector;
import com.globalsight.cxe.entity.gitconnector.GitConnectorCacheFile;
import com.globalsight.everest.util.comparator.FileComparator;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.SortUtil;
import com.globalsight.util.StringUtil;

public class GitConnectorHelper
{
    static private final Logger logger = Logger
            .getLogger(GitConnectorHelper.class);
    
    private static final String GIT_CONNECTOR = "GitConnector";
    private GitConnector gc = null;
    
    final SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() 
	{
		public void configure(Host host, Session session) 
		{
			try 
			{
				String privateKeyFile = gc.getPrivateKeyFile().replaceAll("\\\\", "/");
				if(StringUtil.isNotEmpty(privateKeyFile))
				{
					JSch jsch = getJSch(host, FS.DETECTED);
					jsch.addIdentity(privateKeyFile);
				}
			} 
			catch (JSchException e)
			{
				logger.error(e);
			}
			
			java.util.Properties config = new java.util.Properties(); 
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			
			String password = gc.getPassword();
			if(StringUtil.isNotEmpty(password))
			{
				MyUserInfo ui = new MyUserInfo(gc.getPassword());
				session.setUserInfo(ui);
			}
		}
	};
	
	public static class MyUserInfo implements UserInfo
	{
		String passphrase;
		
		public MyUserInfo(String passphrase) 
		{
		    this.passphrase = passphrase;
		}

		public String getPassphrase() 
		{
			return passphrase;
		}

		public String getPassword() 
		{
			return null;
		}
		
		public boolean promptPassphrase(String message) 
		{
			return true;
		}

		public boolean promptPassword(String message) 
		{
			return true;
		}
		
		public boolean promptYesNo(String str) 
		{
			return true;
		}

		public void showMessage(String message) 
		{
			System.out.println(message);
		}
	}

    public GitConnectorHelper(GitConnector gc)
    {
        this.gc = gc;
    }

    public File getGitFolder()
    {
    	File docFolder = AmbFileStoragePathUtils.getCxeDocDir(gc.getCompanyId());
		String gitFolderStr = docFolder + File.separator + GIT_CONNECTOR
				+ File.separator + gc.getName() + "_" + gc.getId();
        return new File(gitFolderStr);
    }
    
    public CredentialsProvider getCredentialsProvider()
    {
		return new UsernamePasswordCredentialsProvider(gc.getUsername(), gc.getPassword());
    }
    
	public void gitConnectorClone() throws InvalidRemoteException,
			TransportException, GitAPIException, NotSupportedException,
			org.eclipse.jgit.errors.TransportException, URISyntaxException
    {
    	String url = gc.getUrl();
    	CloneCommand cloneCommand = Git.cloneRepository();
		cloneCommand.setURI(gc.getUrl()).setBranch(gc.getBranch())
				.setDirectory(getGitFolder());
    	if(url.startsWith("http"))
    	{
    		cloneCommand.setCredentialsProvider(getCredentialsProvider());
    	}
    	else
    	{
    		cloneCommand.setTransportConfigCallback(new TransportConfigCallback() 
    		{
    			public void configure(Transport transport) 
    			{
    				SshTransport sshTransport = (SshTransport) transport;
    				sshTransport.setSshSessionFactory(sshSessionFactory);
    			}
    		});
    	}
    	cloneCommand.call().close();
    }
    
	public void gitConnectorPull() throws IOException,
			WrongRepositoryStateException, InvalidConfigurationException,
			DetachedHeadException, InvalidRemoteException, CanceledException,
			RefNotFoundException, NoHeadException, TransportException,
			GitAPIException
    {
    	FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
    	repositoryBuilder.setMustExist(true);
    	repositoryBuilder.setGitDir(new File(getGitFolder() + File.separator + ".git"));
    	Repository repository = repositoryBuilder.build();
    	
    	Git git = new Git(repository);
    	Set<String> remoteNames = repository.getRemoteNames();
    	String remoteName = "";
    	for(String name: remoteNames)
    	{
    		remoteName = name;
    	}
    	
    	String url = gc.getUrl();
    	PullCommand pullCommand = git.pull();
    	pullCommand.setRemote(remoteName).setRemoteBranchName(gc.getBranch());
    	if(url.startsWith("http"))
    	{
    		pullCommand.setCredentialsProvider(getCredentialsProvider());
    	}
    	else
    	{
    		pullCommand.setTransportConfigCallback(new TransportConfigCallback() 
    		{
    			public void configure(Transport transport) 
    			{
    				SshTransport sshTransport = (SshTransport) transport;
    				sshTransport.setSshSessionFactory(sshSessionFactory);
    			}
    		});
		}
    	pullCommand.call();
    	repository.close();
    }
    
	public void gitConnectorPush(GitConnectorCacheFile cacheFile)
			throws InvalidRemoteException, TransportException, GitAPIException,
			IOException
    {
		FileUtil.copyFile(new File(cacheFile.getSrcFilePath()), new File(
				cacheFile.getDstFilePath()));
    	
    		FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
    		repositoryBuilder.setMustExist(true);
    		repositoryBuilder.setGitDir(new File(getGitFolder() + File.separator + ".git"));
    		Repository repository = repositoryBuilder.build();
    		
    		Git git = new Git(repository);
    		String filePath = cacheFile.getFilePath();
    		
    		DiffCommand diffCommand = git.diff();
    		List<DiffEntry> diffEntrys = diffCommand.call();
    		filePath = filePath.replaceAll("\\\\", "/");
    		boolean changed = false;
    		for(DiffEntry diffEntry:diffEntrys)
    		{
    			if(diffEntry.getNewPath().equals(filePath))
    			{
    				changed = true;
    				break;
    			}
    		}
    		if(!changed)
    		{
    			repository.close();
    			return;
    		}
    		
    		AddCommand addCommand = git.add();
    		addCommand.addFilepattern(filePath);
    		addCommand.call();
    		
    		CommitCommand commitCommand = git.commit();
    		commitCommand.setMessage("GlobalSight Translation");
    		String username = "GlobalSight";
    		String email = "";
    		if(!StringUtil.isEmpty(gc.getUsername()))
    		{
    			username = gc.getUsername();
    		}
    		if(!StringUtil.isEmpty(gc.getEmail()))
    		{
    			email = gc.getEmail();
    		}
    		commitCommand.setCommitter(username, email);
    		commitCommand.setAuthor(username, email);
    		commitCommand.call();
    		
    		Set<String> remoteNames = repository.getRemoteNames();
    		String remoteName = "";
    		for(String name: remoteNames)
    		{
    			remoteName = name;
    		}
    		
    		String url = gc.getUrl();
    		PushCommand pushCommand = git.push();
    		pushCommand.setRemote(remoteName);
    		if(url.startsWith("http"))
    		{
    			pushCommand.setCredentialsProvider(getCredentialsProvider());
    		}
    		else 
    		{
    			pushCommand.setTransportConfigCallback(new TransportConfigCallback() 
        		{
        			public void configure(Transport transport) 
        			{
        				SshTransport sshTransport = (SshTransport) transport;
        				sshTransport.setSshSessionFactory(sshSessionFactory);
        			}
        		});
			}
    		pushCommand.call();
    		repository.close();
    }
    
    public String getGitConnectorFilesJson()
    {
    	List<GitConnectorFile> gitConnectorFiles = getGitConnectorFiles();
    	return getJSON(gitConnectorFiles);
    }
    
    private List<GitConnectorFile> getGitConnectorFiles()
    {
    	List<GitConnectorFile> gitConnectorFiles = new ArrayList<GitConnectorFile>();
        File folder = getGitFolder();
        File[] files = folder.listFiles();
        Arrays.sort(files);
        for (File file : files)
        {
            if (file.getName().equals(".git"))
                continue;
            
            GitConnectorFile gitConnectorFile = new GitConnectorFile(file);
            
            if(file.isFile())
            {
            	gitConnectorFiles.add(gitConnectorFile);
            	continue;
            }
                
	        setChildren(gitConnectorFile, file);

            if (gitConnectorFile.getChildren() != null)
            {
            	gitConnectorFiles.add(gitConnectorFile);
            }
        }

        return gitConnectorFiles;
    }
    
    @SuppressWarnings("unchecked")
	private void setChildren(GitConnectorFile p_gitConnectorFolder, File p_folder)
    {
        if (FileUtil.isEmpty(p_folder))
            return;

        List<GitConnectorFile> children = new ArrayList<GitConnectorFile>();
        File[] files = p_folder.listFiles();
        SortUtil.sort(files, new FileComparator(0, null, true));
        for (File file : files)
        {
            GitConnectorFile gitConnectorFile = new GitConnectorFile(file);
            if (file.isDirectory() && !FileUtil.isEmpty(file))
            {
                setChildren(gitConnectorFile, file);
            }
            children.add(gitConnectorFile);
        }
        p_gitConnectorFolder.setChildren(children);
    }
    
    private String getJSON(List<GitConnectorFile> p_list)
    {
        if (p_list == null || p_list.size() == 0)
        {
            return "[]";
        }

        StringBuilder result = new StringBuilder();
        result.append("[");
        for (GitConnectorFile GitConnectorFile : p_list)
        {
            result.append(GitConnectorFile.toJSON()).append(", ");
        }
        result.delete(result.length() - 2, result.length());
        result.append("]");
        return result.toString();
    }
}
