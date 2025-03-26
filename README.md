MCC-Beta v1

This application is being developed for creating full channel backups including channelMetadata (stopped being included in the standard channel export in Mirth 3.5.1).

2/7/2025 - First successful test of exporting channels and appending channelMetadata
2/8/2025 - First full export successfully tested. Exports full channel information, exports channelMetadata, and creates a full channel export in ".xml" format
2/11/2025 - Added first rough GUI. Contains an "Export Channels" button to trigger the channelExport. Includes log text area that updates post export completion
2/18/2025 - Added logic to do a full channel export with CodeTemplateLibraries and metadata
3/13/2025 - Fully coded application that exports channels, code template libraries, and full Mirth exports. Added themes, several commands for updating passwords and enabling backups. Packaged with the MCC-SIDEKICK.jar file for running automatic backups. Tested on 15+ clients, 2 mirth blowouts, and 8 scheduled Mirth backups
3/25/2025 - Added forced disconnects after Mirth Database queries/changes to avoid Mirth service hanging. Added the full list of users to "CHECK UN" button and update Mirth password queries. Changed "RESET UN/PW" and update command to update only the selected Mirth username
3/26/2025 - Fixed issue where channels can have "bad" characters that get translated in the <script> tags of the XML. Set up 2 manual lists, one with bad chars and the other with what to replace them with
