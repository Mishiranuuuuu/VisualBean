using System;
using System.Diagnostics;
using System.IO;

class Launcher
{
    static void Main()
    {
        if (File.Exists("Game.jar"))
        {
            ProcessStartInfo startInfo = new ProcessStartInfo();
            startInfo.FileName = "javaw";
            startInfo.Arguments = "-jar Game.jar";
            startInfo.UseShellExecute = false;
            startInfo.CreateNoWindow = true; // Don't show console
            
            try 
            {
                Process.Start(startInfo);
            }
            catch (Exception)
            {
                // Fallback if javaw is not in PATH
                System.Windows.Forms.MessageBox.Show("Java is not found. Please install Java.", "Error", System.Windows.Forms.MessageBoxButtons.OK, System.Windows.Forms.MessageBoxIcon.Error);
            }
        }
        else
        {
            // Simple error
            Console.WriteLine("Game.jar not found!");
            Console.Read();
        }
    }
}
