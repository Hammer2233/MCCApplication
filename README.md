MCC-Beta v1

This application is being developed for creating full channel backups including channelMetadata (stopped being included in the standard channel export in Mirth 3.5.1).

2/7/2025 - First successful test of exporting channels and appending channelMetadata
2/8/2025 - First full export successfully tested. Exports full channel information, exports channelMetadata, and creates a full channel export in ".xml" format
2/11/2025 - Added first rough GUI. Contains an "Export Channels" button to trigger the channelExport. Includes log text area that updates post export completion
2/18/2025 - Added logic to do a full channel export with CodeTemplateLibraries and metadata
2/25/2025 - Added logic for exporting full configuration. Ability to change backup directories and mirth db locations. Added ability to check current mirth username
2/27/2025 - Added more defined GUI. Added checks for service status before running commands. Exported in JAVA 1.8 for an expanded audience
