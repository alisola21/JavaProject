package piscina;

import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.format.DateTimeParseException;

/* GESTIONEPISCINA: classe in cui sono raccolti i metodi per la gestione degli ingressi della piscina, in particolare
    * metodo AggiungiIngresso: chiede all'utente di inserire la data dell'ingresso (può essere quella attuale o un'altra) e,
       dopo aver controllato la temperatura, inserisce l'ingresso che può essere di due tipi: ingresso utente Abbonato e ingresso utente non abbonato
    * metodo IngressiGiornalieri: permette di visualizzare tutti gli ingressi di uno specifico giorno.
    * metodo IngressiMensiliOrdinati: visualizza gli ingressi di uno specifico mesi ordinati in ordine crescente
    * metodo IngressiUtenteAbbonato: inserendo il nome e il cognome dell'utente, restituisce tutti gli ingressi di quello specifico utente abbonato
    * metodo IncassiMensili: visualizza gli incassi di uno specifico mese inserito dall'utente
    * metodo IngressiAbbonamentoMensili: visualizza l'elenco degli ingressi degli utenti abbonati in uno specifico mese

* Oltre a questi metodi, sono presenti alcuni metodi ausiliari, che vengono utilizzati nei metodi sopracitati
    * metodo chiediData: chiede all'utente di inserire la data dell'ingresso e ne controlla la correttezza
    * metodo ChiusuraPiscina: controlla che la data inserita non corrisponda ai giorni di chiusura settimanali
      (domenica e lunedi'), al periodo di chiusura per covid (marzo-maggio 2020/ ottobre 2020-maggio 2021)
    * metodo inserisciMese: permette all'utente di inserire il mese dell'ingresso e ne controlla la correttezza
    * metodi controlloMese e controlloGiornoMese: controlla che l'utente abbia inserito lo 0 nei mesi compresi tra
      gennaio e settembre e nei giorni 1-9.
    * metodo controlloTemperatura: controlla, negli ingressi inseriti dopo marzo 2020, che la temperatura dell'utente sia > 37
    * metodo controlloeta: controlla la correttezza dell'eta' inserita dall'utente quando si inserisce un ingresso di un utente abbonato
* */

public class GestionePiscina {

    //variabili d'istanza
    private Vector<Ingressi> IngressiTOT;
    private static Scanner input = new Scanner(System.in);
    private static DateTimeFormatter formattaData = DateTimeFormatter.ofPattern("d/MM/yyyy");

    private static LocalDate chiusura1 = LocalDate.parse("2020-03-10");
    private static LocalDate apertura1 = LocalDate.parse("2020-07-01");
    private static LocalDate chiusura2 = LocalDate.parse("2020-10-20");
    private static LocalDate apertura2 = LocalDate.parse("2021-05-25");


    public GestionePiscina(Vector v1) {
        this.IngressiTOT = v1;
    }


    // creo un'interfaccia Comparator per gestire l'ordinamento delle date


    // 1 nello switch del main Piscina
    public void aggiungiIngresso() {
        //chiedo di inserire la data e ne controllo la validità
        LocalDate dataIngresso = chiediData();
        boolean DataOK = true;
        char scelta;
        boolean temperaturaOk = true;
        do {
            try {
                //verifico l'apertura della piscina. Se la piscina e' chiusa, richiamo l'eccezione apposita
                boolean piscinaChiusa = ChiusuraPiscina(dataIngresso);
                if (piscinaChiusa) {
                    //imposto a false per bloccare il do while
                    DataOK = false;
                    throw new PiscinaChiusaException(dataIngresso);
                } else {
                    System.out.println("La piscina e' aperta! E' possibile procedere con l'inserimento dell'ingresso!");
                    //blocco il do-while
                    DataOK = false;
                    if ((dataIngresso.isAfter(apertura1) && dataIngresso.isBefore(chiusura2)) ||
                            (dataIngresso.isAfter(apertura2))) {
                        System.out.print("Prima di inserire l'ingresso e' necessario controllare la temperatura dell'utente.\nInserire la temperatura\n");
                        double temperatura = input.nextDouble();
                        temperaturaOk = controllaTemperatura(temperatura);
                    }
                    if (temperaturaOk) {
                        System.out.println("Premere A se l'ingresso e' di un utente ABBONATO o N se non e' abbonato");
                        scelta = input.next().charAt(0);
                        switch (scelta) {
                            case 'A':
                            case 'a':
                                System.out.println("Inserire il nome dell'utente");
                                String nome = input.nextLine();
                                //rimuovo lo spazio dopo l'inserimento del nome
                                nome = input.nextLine();
                                System.out.println("Inserire il cognome dell'utente");
                                String cognome = input.nextLine();
                                // creo un nuovo utente abbonato e un nuovo ingresso
                                UtenteAbbonato utenteAbbonato = new UtenteAbbonato(nome, cognome);
                                IngressiAbbonati nuovoIngressoAbbonati = new IngressiAbbonati(dataIngresso, utenteAbbonato);
                                //inserisco l'ingresso appena creato nel vettore IngressiTOT
                                IngressiTOT.add(nuovoIngressoAbbonati);
                                System.out.println("Ingresso inserito!");
                                break;
                            case 'N':
                            case 'n':
                                System.out.println("E' stato selezionato \"utente non abbonato.\"");
                                System.out.println("\nE' possibile usufruire di riduzioni sul prezzo giornaliero\nInserire l'eta' dell'utente");
                                int eta = input.nextInt();
                                if (!controlloEta(eta)) {
                                    System.out.println("E' stata inserita un'eta' sbagliata!");
                                    break;
                                } else {
                                    //creo l'utente non abbonato con prezzo biglietto pari a 0 e lo setto
                                    UtenteNonAbbonato utenteNonAbbonato = new UtenteNonAbbonato(eta);
                                    double prezzo = utenteNonAbbonato.impostaPrezzoBiglietto();
                                    IngressiNonAbbonati nuovoIngressoNonAbbonati = new IngressiNonAbbonati(dataIngresso, utenteNonAbbonato);
                                    nuovoIngressoNonAbbonati.setPrezzo(prezzo);
                                    IngressiTOT.add(nuovoIngressoNonAbbonati);
                                    System.out.println("\nIngresso inserito!");
                                }
                                break;
                            default:
                                throw new InputMismatchException();
                        }
                    } else
                        DataOK = false;
                }
            } catch (PiscinaChiusaException e) {
                System.out.println(e.getMessage());
            } catch (InputMismatchException e) {
                System.out.println("E' stato inserito un carattere errato! Inserire A o N");
                scelta = input.next().charAt(0);
            }
        } while (DataOK);

    }


    // metodo per visualizzare tutti gli ingressi di uno specifico giorno
    // 2 nello switch del main Piscina
    public void IngressiGiornalieri() {
        //var c permette di controllare se NON sono presenti ingressi nel giorno inserito dall'utente
        int c = 0;
        Collections.sort(IngressiTOT, OrdinaIngressi);
        //chiedo la data
        System.out.println("Inserire il giorno di cui visualizzare gli ingressi in formato gg (es. 01)");
        int giornoInserito = input.nextInt();
        System.out.println("Inserire il mese di cui visualizzare gli ingressi in formato MM (es. 03)");
        int meseInserito = input.nextInt();
        System.out.println("Inserire l'anno di cui visualizzare gli ingressi");
        int annoInserito = input.nextInt();
        //controllo che il mese abbia lo 0 iniziale se compreso tra gennaio
        // e settembre e costruisco la data parsando la stringa ingrMese
        String ingrMese = controllaGiornoMese(giornoInserito, meseInserito, annoInserito);
        LocalDate ingressiGiornalieri = LocalDate.parse(ingrMese, formattaData);
        System.out.println("Ingressi del giorno " + ingressiGiornalieri);
        //scorro il vettore di ingressi e controllo le date degli ingressi
        for (Ingressi i : IngressiTOT) {
            LocalDate dataNegliIngressi = i.getData();
            if (dataNegliIngressi.equals(ingressiGiornalieri)) {
                System.out.println(i);
                c++;
            }
        }
        //stampo un messaggio nel caso in cui non ci siano ingressi inseriti in quella giornata
        if (c == 0) {
            System.out.println("Non sono presenti ingressi nel giorno inserito!");
        }
    }

    Comparator<Ingressi> OrdinaIngressi = new Comparator<Ingressi>() {
        @Override
        public int compare(Ingressi i1, Ingressi i2) {
            return i1.getData().compareTo(i2.getData());
        }
    };

    //metodo per visualizzare tutti gli ingressi di uno specifico mese
    // 3 nello switch del main Piscina
    public void IngressiMensiliOrdinati() {
        int c = 0;
        LocalDate ingressiMeseSpecifico = inserisciMese();
        //ordino il vettore
        Collections.sort(IngressiTOT, OrdinaIngressi);
        for (Ingressi ingresso : IngressiTOT) {
            LocalDate dataIngresso = ingresso.getData();
            if (dataIngresso.getYear() == ingressiMeseSpecifico.getYear()) {
                if (dataIngresso.getMonthValue() == ingressiMeseSpecifico.getMonthValue()) {
                    System.out.println(ingresso);
                    c++;
                }
            }
            ingressiMeseSpecifico = ingressiMeseSpecifico.plusDays(1);
        }
        if (c == 0) {
            System.out.println("Non sono presenti ingressi nel mese inserito");
        }
    }


    // metodo per visualizzare tutti gli ingressi di uno specifico utente abbonato
    // 4 nello switch del main Piscina
    public void IngressiUtenteAbbonato() {
        int c = 0;
        Collections.sort(IngressiTOT, OrdinaIngressi);
        System.out.println("Inserisci il nome dell'utente");
        // cerco di risolvere il bug dello spazio usando nextLine()
        // altrimenti stampa "inserisci nome e inserisci cognome" in una sola riga
        String nomeUtente = input.next() + input.nextLine();
        System.out.println("Inserisci il cognome dell'utente");
        String cognomeUtente = input.nextLine();
        IngressiAbbonati iA = null;
        System.out.println("Ingressi effettuati dall'utente " + nomeUtente + " " + cognomeUtente + " nella piscina:");
        for (Ingressi i : IngressiTOT) {
            //controllo se un ingresso e' stato effettuato da un utente ABBONATO
            if (i instanceof IngressiAbbonati) {
                iA = (IngressiAbbonati) i;
                UtenteAbbonato utente = iA.getUtente();
                //recupero con le apposite get il nome e il cognome dell'utente
                String nomeUtenteAbbonato = utente.getNome();
                String cognomeUtenteAbbonato = utente.getCognome();
                //controllo se il nome e il cognome dell'utente inseriti corrispondono
                //con il nome e il cognome dell'utente di cui e' registrato l'ingresso
                boolean controllo = utente.equals(nomeUtente, cognomeUtente);
                if (controllo) {
                    System.out.println(iA);
                    c++;
                }
            }
        }
        if (c == 0) {
            System.out.println("Non sono stati registrati accessi da parte dell'utente " + nomeUtente + " " + cognomeUtente);
        }
    }


    // metodo per visualizzare l'elenco degli incassi giornalieri di uno specifico mese
    // 5 nello switch del main Piscina
    public void IncassiMensili() {
        //ordino il vettore
        Collections.sort(IngressiTOT, OrdinaIngressi);
        //interi per contare il numero di ingressi ridotti e gli ingressi complessivi
        //double per contare l'incasso di ciascun giorno
        int contaRidotti = 0;
        int contaIngressiMese = 0;
        double incassoGiornaliero = 0;
        int incassoTOT = 0;
        //richiedo mese/anno all'utente
        //meseSpecifico contiene una data composta da 01/mese inserito/anno inserito
        LocalDate meseSpecifico = inserisciMese();
        Month mese = meseSpecifico.getMonth();
        int anno = meseSpecifico.getYear();
        //controllo il numero di giorni del mese, mi serviranno per il ciclo for
        YearMonth annoEMese = YearMonth.of(anno, mese);
        int giornidelMese = annoEMese.lengthOfMonth();
        //stampe
        String stampaTitolo = mese.getDisplayName(TextStyle.FULL, Locale.ITALIAN) + " " + anno;
        System.out.println("Incassi del mese " + stampaTitolo);
        //j = 1 perché useremo j per stampare il numero del giorno del mese
        for (int j = 1; j < giornidelMese + 1; j++) {
            incassoGiornaliero = 0;
            for (Ingressi i : IngressiTOT) {
                //controllo se la data dell'ingresso i corrisponde a quella del mese
                if (i.getData().equals(meseSpecifico)) {
                    //controllo che l'ingresso sia di un NON ABBONATO
                    if (i instanceof IngressiNonAbbonati) {
                        contaIngressiMese++;
                        //downcast per usare le get della classe UtenteNonAbbonato
                        //e contare il numero ridotti
                        IngressiNonAbbonati ingressoNonAbbonato = (IngressiNonAbbonati) i;
                        UtenteNonAbbonato uNonAbbonato = ingressoNonAbbonato.getUtenteNA();
                        if (uNonAbbonato.getRidottoBambiniEAnziani() || uNonAbbonato.getStudente()) {
                            contaRidotti++;
                        }
                        //uso la getPrezzoBiglietto per sapere il prezzo del biglietto di quello
                        //specifico ingresso
                        double bigliettoUtente = uNonAbbonato.getPrezzoBiglietto();
                        incassoGiornaliero += bigliettoUtente;
                        incassoTOT += bigliettoUtente;
                    }
                }
            }
            //scorro i giorni del mese aggiungendo con il metodo plusDays(1) un giorno alla data meseSpecifico
            //e stampo gli incassi del giorno preso in considerazione alla i-esima iterazione
            meseSpecifico = meseSpecifico.plusDays(1);
            System.out.println("Giorno " + j + ":\t\t Incasso: " + incassoGiornaliero + " euro");
        }
        //stampo infine gli ingressi totali del mese e gli incassi complessivi
        System.out.println("\nNumero totale di ingressi: " + contaIngressiMese + " di cui " + contaRidotti + " con prezzo ridotto\n" +
                "Incassi totali del mese: " + incassoTOT);
    }

    // metodo per visualizzare l'elenco con il numero degli ingressi in abbonamento giornalieri di uno specifico mese
    // 6 nello switch del main Piscina
    public void IngressiAbbonatiMensili() {
        Collections.sort(IngressiTOT, OrdinaIngressi);
        int numeroIngressi = 0;
        int contaIngressi = 0;
        //creo una data "fittizia" con giorno impostato a 01, mese inserito dall'utente, anno
        LocalDate meseSpecifico = inserisciMese();
        //salvo mese e anno in variabili apposite
        Month mese = meseSpecifico.getMonth();
        int anno = meseSpecifico.getYear();
        YearMonth annoEMese = YearMonth.of(anno, mese);
        //recupero il numero di giorni del mese
        int giornidelMese = annoEMese.lengthOfMonth();

        //stampo l'intestazione
        String stampaTitolo = mese.getDisplayName(TextStyle.FULL, Locale.ITALIAN) + " " + anno;
        System.out.println("Elenco dei soli ingressi di abbonati del mese " + stampaTitolo);

        //j = 1 perché useremo j per stampare il numero del giorno del mese
        for (int j = 1; j < giornidelMese + 1; j++) {
            numeroIngressi = 0;
            for (Ingressi i : IngressiTOT) {
                if (i.getData().equals(meseSpecifico)) {
                    //controllo se la data inserita matcha quella dell'ingresso salvato e incremento i counter
                    if (i instanceof IngressiAbbonati) {
                        IngressiAbbonati ingressoAbbonati = (IngressiAbbonati) i;
                        numeroIngressi++;
                        contaIngressi++;
                    }
                }
            }
            meseSpecifico = meseSpecifico.plusDays(1);
            System.out.println("Giorno " + j + ":\t\t" + numeroIngressi);
        }
        if (contaIngressi == 0) {
            System.out.println("\nNon sono presenti ingressi nel mese inserito");
        } else {
            System.out.println("\nSono entrate " + contaIngressi + " persone abbonate nel mese di "
                    + mese.getDisplayName(TextStyle.FULL, Locale.ITALIAN) + " " + anno);
        }
    }


    // stampa degli elementi nel vettore
    public void visualizzaIngresso() {
        System.out.println("----------------Elenco totale ingressi-----------------");
        for (Object ingresso : IngressiTOT) {
            System.out.println(ingresso);
        }
    }


    /* -------------- da qui METODI AUSILIARI -------------------*/

    /* metodo ausiliario per chiedere la data all'utente che controlla la correttezza della data e che la data inserita
     non sia posteriore al giorno attual*/
    private static LocalDate chiediData() {
        LocalDate data = null;
        boolean ok = true;
        char scelta;
        System.out.println("Vuoi inserire un ingresso nel giorno attuale? [S] [N]");
        do {
            try {
                scelta = input.next().charAt(0);
                switch (scelta) {
                    case 'S':
                    case 's':
                        data = LocalDate.now();
                        ok = false;
                        break;
                    case 'N':
                    case 'n':
                        System.out.println("Inserisci una data in formato DD/MM/YYYY");
                        String d1 = input.next();
                        data = LocalDate.parse(d1, formattaData);
                        input.nextLine();
                        if (d1 == null) {
                            throw new DataErrataException();
                        }
                        if (data.isAfter(LocalDate.now())) {
                            System.out.println("Data posteriore al giorno attuale.");
                            throw new DataErrataException();
                        } else {
                            ok = false;
                        }
                        break;
                    default:
                        throw new InputMismatchException();
                }
            } catch (InputMismatchException e) {
                System.out.println("Inserisci S o N");
            } catch (DateTimeParseException e) {
                System.out.println("Data errata!");
            } catch (DataErrataException e) {
                System.out.println("Inserisci una data corretta!");
            }
        } while (ok);
        return data;
    }

    /* controlla che la data inserita non corrisponda ai giorni di chiusura settimanali
       (domenica e lunedi'), al periodo di chiusura per covid (marzo-maggio 2020/ ottobre 2020-maggio 2021)*/
    private static boolean ChiusuraPiscina(LocalDate data) {
        boolean chiusura = false;
        //definisco i periodi di chiusura
        try {
            //controllo che la data inserita non cada di domenica o di lunedi'
            //giorni di chiusura della piscina
            if ((data.getDayOfWeek().equals(DayOfWeek.SUNDAY) ||
                    data.getDayOfWeek().equals(DayOfWeek.MONDAY))) {
                chiusura = true;
                throw new PiscinaChiusaException();
            }
            //date di chiusura piscina per covid
            if (((data.isAfter(chiusura1) && data.isBefore(apertura1)) ||
                    (data.isAfter(chiusura2) && data.isBefore(apertura2)))) {
                chiusura = true;
                throw new PiscinaChiusaException();
            }
            //si assume che la piscina abbia aperto nel 2015
            if ((data.getYear() < 2015)) {
                chiusura = true;
                throw new PiscinaChiusaException();
            }
        } catch (PiscinaChiusaException e) {
        }
        return chiusura;
    }


    /* metodo ausiliario che controlla la temperatura dell'utente prima di entrare in piscina
    (introdotta per l'emergenza Covid-19)*/
    private static boolean controllaTemperatura(double temperatura) {
        boolean temperaturaOK = true;
        boolean ok = true;
        do {
            try {
                if ((temperatura >= 37) && (temperatura <= 41)) { // se la temperatura e' >= 37, non e' consentito l'ingresso
                    //temperaturaOK = false;
                    System.out.println("La temperatura dell'utente e' superiore a 37 gradi e, " +
                            "come descritto nel protocollo anti-covid, non e' consentito l'accesso");
                    temperaturaOK = false;
                    break;
                }
                // se la temperatura e' troppo alta o troppo bassa, si inserisce nuovamente la temperatura
                if ((temperatura <= 34) || (temperatura > 41)) {
                    System.out.println("Il valore della temperatura non e' corretto, misurare nuovamente la temperatura");
                    temperatura = input.nextDouble();
                    ok = false;
                } else {
                    System.out.println("La temperatura dell'utente e' inferiore a 37 gradi, si può procedere con la registrazione");
                    ok = true;
                    temperaturaOK = true;
                }
            } catch (InputMismatchException e) {
                System.out.println("Riprovare con un altro carattere.");
            }
        } while (!ok);
        return temperaturaOK;
    }

    private static LocalDate inserisciMese() {
        LocalDate ingressiDelMese = null;
        System.out.println("Inserire il mese di cui si vogliono visualizzare gli ingressi in formato mm (es. 01 per gennaio, 02 per febbraio)");
        int meseInserito = input.nextInt();
        System.out.println("Inserire l'anno di cui si vogliono visualizzare gli ingressi");
        int annoInserito = input.nextInt();
        //input.nextLine per evitare che nello scanner rimanga in buffer il \n
        input.nextLine();
        //costruisco una Localdate ad hoc con 01 + mese + anno
        String ingrMese = controllaMese(meseInserito, annoInserito);
        ingressiDelMese = LocalDate.parse(ingrMese, formattaData);
        return ingressiDelMese;
    }


    // Metodo ausiliario che controlla che l'utente abbia inserito lo 0 nei mesi compresi tra gennaio e settembre
    // se non lo ha messo, viene aggiunto in automatico
    private static String controllaMese(int meseInserito, int annoInserito) {
        String ingrMese = "";
        if (meseInserito >= 1 && meseInserito <= 9) {
            ingrMese = "01/0" + meseInserito + "/" + annoInserito;
        } else {
            ingrMese = "01/" + meseInserito + "/" + annoInserito;
        }
        return ingrMese;
    }

    // Metodo ausiliario che controlla che l'utente abbia inserito lo 0 nei mesi compresi tra gennaio e settembre
    // e nei giorni tra 1 e 9 se non lo ha messo, viene aggiunto in automatico
    private static String controllaGiornoMese(int giornoInserito, int meseInserito, int annoInserito) {
        String ingrMese = "";
        if ((meseInserito >= 1 && meseInserito <= 9) || ((giornoInserito >= 1) &&
                giornoInserito <= 9)) {
            ingrMese = "0" + giornoInserito + "/0" + meseInserito + "/" + annoInserito;
        } else {
            ingrMese = giornoInserito + meseInserito + "/" + annoInserito;
        }
        return ingrMese;
    }

    //Metodo che controlla la correttezza dell'eta': vengono scartate età inferiori a 0 o troppo elevate
    private static boolean controlloEta(int eta) {
        boolean etaOK = true;
        if ((eta <= 0) || (eta >= 150)) {
            etaOK = false;
        }
        return etaOK;
    }
}