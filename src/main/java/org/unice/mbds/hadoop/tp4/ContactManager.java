package org.unice.mbds.hadoop.tp4;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.cli.*;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Gael on 01/12/2015.
 */
public class ContactManager {

    private static Logger mongoLogger = Logger.getLogger("com.mongodb");


    private MongoClient client = new MongoClient();
    private MongoDatabase db = client.getDatabase("tp4");
    private MongoCollection coll = db.getCollection("contacts");

    public void addContact(String prenom, String nom, String email) {
        Document doc = new Document("prenom", prenom).append("nom", nom).append("email", email);

        coll.insertOne(doc);
    }


    public void deleteContact(String email) {
        Document doc = new Document("email", email);

        coll.deleteOne(doc);
    }


    public void findContact(String[] values) {

        Document doc = new Document();
        Document or = new Document();
        List<String> nomPrenom = new ArrayList<>();
        List<String> emails = new ArrayList<>();

        for (String val : values) {
            if (val.contains("@")) {
                emails.add(val);
            } else {
                nomPrenom.add(val);
            }
        }

        doc.append("email", new Document("$in", emails));
        doc.append("nom", new Document("$in", nomPrenom));
        doc.append("prenom", new Document("$in", nomPrenom));

        ArrayList<Document> document = new ArrayList<>();
        document.add(doc);

        or.append("$or", document);

        System.out.println("Résultats pour " + Arrays.toString(values) + ":\n ");
        System.out.println("\n");
        coll.find(or).forEach(new Block<Document>() {
            @Override
            public void apply(Document d) {
                System.out.println(d.getString("nom") + ", " + d.getString("prenom") + ", " + d.getString("email") + "\n");
            }
        });

    }

    public static void main(String[] args) {

        mongoLogger.setLevel(Level.SEVERE);

        Options opts = new Options();

        opts.addOption("a", "add", true, "Permet d'ajouter un contact dans la base de données." +
                "\nFormat : prénom,nom,em@il (séparés par une virgule)");
        opts.addOption("d", "del", true, "Permet de supprimer un contact de la base de données." +
                "\nFormat : em@il");
        opts.addOption("f", "find", true, "Permet de trouver un contact dans la base de données." +
                "\nFormat : chaîne1,chaîne2,...,chaîneN (séparés par une virgule)" +
                "\nChaque valeur contenant \"@\" permettra de chercher un email, chaque autre valeur correspond à un prénom ou un nom.");

        CommandLineParser clp = new DefaultParser();
        CommandLine cmd = null;

        try {
            cmd = clp.parse(opts, args);
        } catch (ParseException e) {
            System.err.println("Erreur dans le parsing des arguments.");
            showHelp(opts);
        }

        ContactManager cm = new ContactManager();

        if (cmd.hasOption("a")) {
            String[] values = cmd.getOptionValue("a").split(",");

            if (values.length == 3) {
                cm.addContact(values[0], values[1], values[2]);
            } else {
                System.err.println("Problème dans le format des paramètres");
                showHelp(opts);
            }
        } else if (cmd.hasOption("d")) {
            String[] value = cmd.getOptionValue("d").split(",");

            if (value.length == 1 && value[0].contains("@")) {
                cm.deleteContact(value[0]);
            } else {
                System.err.println("Il est seulement autorisé de supprimer un contact par son adresse email.");
                showHelp(opts);
            }

        } else if (cmd.hasOption("f")) {
            String[] values = cmd.getOptionValue("f").split(",");

            cm.findContact(values);
        } else {
            System.err.println("Le paramètre spécifié n'existe pas.");
            showHelp(opts);
        }
    }

    public static void showHelp(Options opts) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("cm", opts);
    }
}
