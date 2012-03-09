using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace pegasus.eventbus.amqp
{
    public struct Exchange
    {
        public string Name;
        public ExchangeType Type;
        public bool IsDurable;


        public Exchange(string name, ExchangeType type, bool isDurable)
        {
            this.Name = name;
            this.Type = type;
            this.IsDurable = isDurable;
        }


        public override string ToString()
        {
            return string.Format("Exchange [Name={0}]", this.Name);
        }

        public override int GetHashCode()
        {
            int hash = 17;

            unchecked
            {
                hash = 23 * hash + (this.IsDurable ? 1231 : 1237);
                hash = 23 * hash + (string.IsNullOrWhiteSpace(this.Name) ? 0 : this.Name.GetHashCode());
                hash = 23 * hash + this.Type.GetHashCode();
            }

            return hash;
        }

        public override bool Equals(object obj)
        {
            if (object.ReferenceEquals(this, obj)) { return true; }
            if (null == obj) { return false; }
            if (false == obj is Exception) { return false; }

            Exchange ex = (Exchange)obj;

            if (this.GetHashCode() == ex.GetHashCode()) { return true; }
            else { return false; }
        }
    }




    public enum ExchangeType { Direct, Topic, Fanout }
}
