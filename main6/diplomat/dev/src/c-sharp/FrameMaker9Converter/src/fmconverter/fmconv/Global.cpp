extern int CSHandleCommand(int command, char *svFile);
__declspec(dllexport) int HandleCommand(int command, char *svFile)
{
	return CSHandleCommand(command, svFile);
}