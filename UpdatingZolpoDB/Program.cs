using DBOperations;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace UpdatingZolPoDBTask
{
    public class Program
    {
        static void Main(string[] args)
        {
            zolpoDB.DeleteAllDBRecords();
            zolpoDB.DeleteCache();
            zolpoDB.StartDB();
        }
    }
}
