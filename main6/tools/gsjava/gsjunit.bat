@echo off
setlocal
setlocal enabledelayedexpansion

set DIR=%~dp0

!DIR!\gsjava org.junit.runner.JUnitCore %*

