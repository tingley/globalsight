# GBS-2590: getFileProfileInfoEx() API needs to escape "&" character

Update file_profile set description=replace(description, '&', '&amp;');
Update file_profile set description=replace(description, '>', '&gt;');
Update file_profile set description=replace(description, '<', '&lt;');
Update file_profile set description=replace(description, '\"', '&quot;');