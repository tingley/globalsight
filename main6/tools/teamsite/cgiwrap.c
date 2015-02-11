/******************************************************************/
/* Adam J. Griff, Ph.D.                                           */
/* Simple CGI Wrapper Program for specific for IW                 */
/* Adapted from:  Nathan Neulinger  <nneul@umr.edu>               */
/* http://download.sourceforge.net/cgiwrap/   from V1.0           */
/******************************************************************/

#include <stdio.h>     /* For passing data into called script */
#include <stdlib.h>    
#include <pwd.h>       /* For getting uids from username */
#include <unistd.h>    /* Stuff for setting uid      */
#include <sys/types.h> /* Types for uid and stuff */
#include <limits.h>   /* Limit on directory length and stuff */
#include <sys/stat.h>  /* For file stating */
#include <sys/time.h>  /* Time for logging stuff */
#include <errno.h>
#include <sys/resource.h>
#include <sys/wait.h>


/***************************************/
/* Main Configuration File for CGIWrap */
/***************************************/


/**/
/**  CGIWrap Security and Compile Options      */
/**  #define to turn on, #undef to turn off    */
/**/


/* Choose which method to run CGI wrap */
#undef SETUID    	/* Server run as root */
#define SETREUID  	/* Server run as other, cgiwrap suid root */
#undef SETRESUID	/* Server run as other, cgiwrap suid root, hpux */

/* Enable Debugging Output */
/* #undef DEBUG */


/* Error if user UID <> script owner ID */
#define CHECK_UID


/* Error if user GID <> script group ID */
#undef CHECK_GID


/* Error if SUID bit of script is set */
#define CHECK_SUID


/* Error if SGID bit of script is set */
#define CHECK_SGID


/* Do not allow subdirectories of cgi-bin dir */
#define NO_SUB_DIRS


/* Sanitize Directory and User Names  */
#define SANITIZE


/* Double check to make sure UID changed */
#define CHECK_RUID


/* Double check to make sure GID changed */
#define CHECK_RGID


/***/
/**   Macro for safely using null strings */
/***/
#define NullCheck(s) ( (s) ? (s) : ("<NULL>") )



/***/
/**   Display the exit value as a message */
/***/
void exitStatus (int exitvalue) {
  if (exitvalue==0) {
    printf ("status=OK\n");
  } else {
    printf ("status=FAIL\n");
  }
}

/***/
/**   Display an error message and exit the program */
/***/
void DoError (char *msg)
{
	extern int errno;
	extern char *sys_errlist[];

	printf ("Content-Type: text/plain\n\n");

	printf (" CGI Wrapper Error: %s \n", msg);
	printf (" Last Error: %s [%d]\n", sys_errlist[errno], errno);

	exitStatus(1);
	exit(1);
}



/***/
/**   Print a debugging string if DEBUG is defined */
/***/
void DEBUG_Msg (char *msg)
{
#ifdef DEBUG
	printf("%s\n", NullCheck(msg));
	fflush(stdout);
#endif
}

void DEBUG_Str (char *msg, char *var)
{
#ifdef DEBUG
	printf("%s '%s'\n", NullCheck(msg), NullCheck(var) );
	fflush(stdout);
#endif
}

void DEBUG_Int (char *msg, int var)
{
#ifdef DEBUG
	printf("%s '%d'\n", NullCheck(msg), var );
	fflush(stdout);
#endif
}


/***/
/**   Copy a string into a newly allocated block of memory */
/***/
char *mystrcpy(char *str)
{
	char *temp;

	temp = (char *)  malloc ( strlen(str) + 1 );
	if (!temp)
	{
		DoError("Couldn't malloc memory for string!");
	}
	strcpy(temp,str);

	return temp;
}



/***/
/**   Extract and return the value portion of a key=value pair found in a string */
/***/
char *GetValue( char *keyword, char *string )
{
	char *returnStr;
	char *dataStr;
	char *tempStr;
	char *theString;
	int i;

	theString = mystrcpy(string);

	returnStr = (char *) 0;
        tempStr = (char *) strtok(theString, "&");
        while (tempStr)
        {
                dataStr = (char *) strstr(tempStr, keyword);
                if ( dataStr == tempStr )
                {
                        returnStr = (char *) strchr (dataStr, '=') + 1;
			tempStr = (char *) 0;
                }
		else
		{
                	tempStr = (char *) strtok( (char *) 0, "&");
		}
        }


DEBUG_Str("   Keyword: ", keyword);
DEBUG_Str("   Value: ", returnStr);
	
	returnStr = mystrcpy(returnStr);
	free(theString);
	return returnStr;
}


/***/
/**  Clean up a username and script name - removing non printables and whitespace  */
/***/
void Sanitize(char *string)
{
	int len, in;

	len = strlen(string);
	if (string) 
	{
		for (in=0; in<len; in++)
		{

			if ( !isprint(string[in]) || isspace(string[in]) )
			{
				string[in] = '_';	
			}
			
#ifdef NO_SUB_DIRS
			if ( string[in] == '/' ) 
			{
				string[in] = '_';
			}
#endif
		}

	}
}

int IWBaseDir (void)
{
      FILE *fileid;
      char basedir[5000];
      int filedes[2];
      int status;
      int wstatus;
      pid_t pstatus;


      status = pipe(filedes);
      if (status != 0) {
	fprintf(stderr, "We got an error number %d.\n", errno);
	perror("KABOOM (thud) ...");
	exitStatus(-1);
	exit(-1);
      }


      pstatus = fork();
      if (pstatus == 0) {
	// ** I am the child! **
	fclose(stdout);
	fclose(stderr);
	dup(filedes[1]);
	dup(filedes[1]);
	execlp("/usr/bin/iwgethome", "iwgethome", NULL);

	puts("Evilness has occurred if this ever prints!");
	exitStatus(-1);
	exit(-1);
      }

      // ** I am (hopefully) the parent! **
      fileid = fdopen(filedes[0], "r");
      fscanf(fileid, "%s", &basedir);
      fclose(fileid);
      // wait4(0, NULL, wstatus, NULL);
      close(filedes[0]);
      close(filedes[1]);

/***/
/**   Change to home directory of that user */
/***/
	//	if ( chdir( user->pw_dir ) )
	if ( chdir(basedir) )
	{
		DoError("Could not chdir to home directory!");
	}
      return 1;
}



/***/
/**   Main program */
/***/
int main (void)
{
	char *userStr;		/* User name */
	char *scrStr;		/* Name of script */
	struct passwd *user;	/* For getting uid from name */
	char *dir;              /* For checking current directory */
	struct stat fileStat;	/* For checking file status */
	int statErr;		/* For checking reason why stat failed */
	int scriptErr;		/* Return code from executing script */
	char *execStr;		/* String that is actually executed */


/***/
/**   Print out the URL used to query this script */
/***/
DEBUG_Msg("Content-Type: text/plain\n\n");
DEBUG_Str("Query_String:", (char *) getenv("QUERY_STRING") );


/***/
/**   Check to make sure a query was actually made */
/***/
// AJG We might need this or another way to get data from put were we include
// AJG the file to execute as a parameter 
//	if ( !getenv("QUERY_STRING") )
//	{
//		DoError("No query string was specified, check your URL.");
//	}

/***/
/**   Find user= and script= in query string */
/***/
DEBUG_Msg("\nRead in user keyword value");
//	userStr = GetValue("user", getenv("QUERY_STRING") );
	userStr = "root";

DEBUG_Msg("\nRead in script keyword value");
//	scrStr = GetValue("script", getenv("QUERY_STRING") );
 scrStr = "GlobalSightImport2.cgi";


/***/
/**   Sanitize user name and script name */
/***/
#ifdef SANITIZE
DEBUG_Str("\nSanitize user name:", userStr);
	Sanitize(userStr);
DEBUG_Str("   Sanitized to:", userStr);
DEBUG_Str("Sanitize script name:", scrStr);
	Sanitize(scrStr);
DEBUG_Str("   Sanitized to:", scrStr);
#endif




/***/
/**   Check if a username was given */
/***/
	if (!userStr || ( strlen( NullCheck(userStr) ) == 0 ) ) 
	{
		DoError("No User Specified");
	}		


/***/
/**   Check if a scriptname was given */
/***/
	if (!scrStr || ( strlen(scrStr) == 0 ) )
	{
		DoError("No Script Specified");
	}


/***/
/**   Lookup username to get UID */
/***/
	if ( !(user = getpwnam(userStr)) )
	{
		DoError("User not found.");
	}


DEBUG_Msg("\nUser Data Retrieved:");
DEBUG_Str("   UserName:",  user->pw_name);
DEBUG_Int("   UID:",       user->pw_uid);
DEBUG_Int("   GID:",       user->pw_gid);
DEBUG_Str("   Directory:", user->pw_dir);


/***/
/**   Change real&effective UIDs to match username and group of that user */
/***/
#ifdef SETUID
	setgid( user->pw_gid );  /* Have to change group first? */
	setuid( user->pw_uid );
#endif

#ifdef SETREUID
	setregid( user->pw_gid, user->pw_gid );
	setreuid( user->pw_uid, user->pw_uid );
#endif

#ifdef SETRESUID
	setresgid( user->pw_gid, user->pw_gid, user->pw_gid );
        setresuid( user->pw_uid, user->pw_uid, user->pw_uid );
#endif


DEBUG_Msg("\nUIDs/GIDs Changed To:");
DEBUG_Int("   RUID:", getuid());
DEBUG_Int("   EUID:", geteuid());
DEBUG_Int("   RGID:", getgid());
DEBUG_Int("   EGID:", getegid()); 


/***/
/**   Check to make sure the RUID actually changed with the setuid call */
/***/
#ifdef CHECK_RUID
	if ( getuid() != user->pw_uid ) 
	{
		DoError("UID could not be changed!");
	}
#endif

/***/
/**   Check to make sure the RGID actually changed with the setgid call */
/***/
#ifdef CHECK_RGID
	if ( getgid() != user->pw_gid )
	{
		DoError("GID could not be changed!");
	}
#endif

	/***/
	/** Change to the IW base directory */
	/***/
	
	IWBaseDir();

/***/
/**   Change to script directory */
/***/
	//	if ( chdir( CGIDIR ) )
	if ( chdir("httpd/iw-bin") )
	{
		DoError("Could not chdir to script directory!");
	}

	
DEBUG_Str("\nCurrent Directory:", (char *) getcwd(0,200) );


/***/
/**   Do a stat or something on the script to execute to make sure it is */
/**   Owned by person who is specified as the username, will prevent */
/**   sym links to other people scripts */
/***/

	if ( statErr = stat(scrStr, &fileStat) )  
	{
		DoError("Could not stat script file!");
	}

DEBUG_Msg("\nResults of stat:");
DEBUG_Int("   File Owner:", (int) fileStat.st_uid);
DEBUG_Int("   File Group:", (int) fileStat.st_gid);	


#ifdef CHECK_UID
	if (fileStat.st_uid != user->pw_uid) 
	{
		DoError("Script does not have same UID");
	}
#endif


#ifdef CHECK_GID
	if (fileStat.st_gid != user->pw_gid)
	{
		DoError("Script does not have same GID");
	}
#endif


#ifdef CHECK_SUID
	if (fileStat.st_mode & S_ISUID)
	{
		DoError("Script is SUID - Will not Execute!");
	}
#endif


#ifdef CHECK_SGID 
	if (fileStat.st_mode & S_ISGID)
	{
		DoError("Script is SGID - Will not Execute!");
	}
#endif



/***/
/**   Prepend "./" onto the script string so it can be executed without */
/**   the current path affecting anything */
/***/
	execStr = (char *) malloc( (size_t) (strlen(scrStr) + 2) );
	strcpy(execStr, "./");
	strcat(execStr, scrStr);


	
/***/
/**   Exec the script - script will need to be run with /bin/sh so it */
/**   can start up perl and other things... */
/***/

DEBUG_Msg("\n\nOutput of script follows:");
DEBUG_Msg("=====================================================");
	scriptErr = system(execStr);
DEBUG_Int("Result of Script =",scriptErr);
 exitStatus(scriptErr);
 return (scriptErr);
}
