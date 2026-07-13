package hk.service;


public class ZoteDialogueService {


    public String[] initialDialogue() {
        return new String[] {
            "Needle is the term used to describe the bladed weapon wielded by Hornet. The needle is attached with a thread of silk.",
            "A self-proclaimed Knight, of no renown. Wields a nail he carved from shellwood, named 'Life Ender.'",
            "Some rare creatures are so weak, so helpless, so inept and so irritating that hunting them gives no pleasure."
        };
    }


    public String precept(int index) {
        String[] precepts = {
            "Precept Fifty Seven: Standing in the rain makes you wet. Also, cold.",
            "Precept Four: If you want a thing done well, do it yourself. Unless the thing is dangerous, in which case, delegate.",
            "Precept Thirty Three: Never try to outrun a faster creature. Instead, stand still and pretend you meant to stop.",
            "Precept Eleven: A great warrior knows when to fight. Equally, a great warrior knows when to run. Most of the time, run.",
            "Precept Twenty: Do not try to befriend your enemies. Though if you manage it, keep them close.",
            "Precept Forty Two: If you cannot defeat an enemy, declare victory anyway and walk away with purpose.",
            "Precept Sixty: Weakness is an affliction. I have never suffered from it.",
            "Precept Eight: A real warrior does not celebrate victories. Unless the victory was particularly impressive, in which case, celebrate loudly.",
        };
        return precepts[index % precepts.length];
    }
}
