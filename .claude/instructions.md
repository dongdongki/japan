core Operational Guidelines
UI Color Specification

Text: The application operates in Dark Mode, so all UI/UX text must be fixed to white, regardless of the text's content or meaning.

Background/Elements: All UI components other than text (backgrounds, buttons, borders, etc.) must use a shade of black or a dark color palette.

Comprehensive Error Resolution Principle

When an issue is identified, do not limit the correction to only the single point of failure found.

You must thoroughly review and modify all directly related code and any other code that performs the same function.

After correction, you must ensure the function is fully and reliably operational through rigorous testing.

💡 Precautions
Codebase Integrity

Maintain strict codebase management to ensure no duplicate source code files exist. (Adhere to the DRY principle).

Removal of Unused Assets

Any unused files or source code within the project must be immediately removed upon discovery. (Dead Code/Asset Cleanup).

🚫 Prohibitions
Forbidden User Communication

Absolutely prohibit any statements that shift the burden of troubleshooting to the user, such as suggesting the user "reinstall the app" or "clear the cache." Issues must only be resolved through code modification.

Forbidden Workflow Steps

Only proceed with the build process; installation is not to be performed. (The AI's role is limited to code modification and build generation, not device installation).