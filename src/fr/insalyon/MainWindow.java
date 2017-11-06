package fr.insalyon;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class MainWindow extends JFrame implements ActionListener {
    JTextField m_searchText;
    JButton m_searchButton;

    JTextArea m_resultArea;

    //public final Dimension DIM_SEARCHBAR = new Dimension (300, 400);

    public MainWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("TrucDeRecherche");

        Container pane = getContentPane();

        JPanel searchBar = new JPanel();
        searchBar.setLayout(new BoxLayout(searchBar, BoxLayout.X_AXIS));
        //searchBar.setMinimumSize(DIM_SEARCHBAR);
        //searchBar.setPreferredSize(DIM_SEARCHBAR);

        JLabel searchLabel = new JLabel("DuckDuckGo : ");
        searchBar.add(searchLabel);

        m_searchText = new JTextField();
        m_searchText.addActionListener(this);
        searchBar.add(m_searchText);

        m_searchButton = new JButton("Rechercher");
        m_searchButton.addActionListener(this);
        searchBar.add(m_searchButton);

        pane.add(searchBar, BorderLayout.PAGE_START);

        m_resultArea = new JTextArea();
        m_resultArea.setLineWrap(true);

        pane.add(m_resultArea, BorderLayout.CENTER);

        pack();
        setSize(1200,700);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == m_searchButton || e.getSource() == m_searchText) {
            JSONArray resultats = recupererResultats(m_searchText.getText());


            m_resultArea.setText(resultats.toString());
        }
    }

    private void extendArray(JSONArray dest, JSONArray src)
            throws JSONException {
        for (int i = 0; i < src.length(); i++) {
            dest.put(src.get(i));
        }
    }

    public JSONArray recupererResultats(String requete) {
        java.util.List<String> liens = null;
        try {
            liens = HTMLContentParser.getListURLForDuckDuckGo(HTTPQueryHandler.queryDuckDuckGo(requete));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        Spotlight s = new Spotlight();

        JSONArray tableOfURIs = new JSONArray();

        for (String lien : liens) {
            try {
                List<String> paragraphs = HTMLContentParser.getParagraphsForDocument(HTTPQueryHandler.getHTML(lien));

                JSONArray URIs = new JSONArray();
                for (String p : paragraphs) {
                    if (p.isEmpty()) {
                        continue;
                    }

                    JSONArray json = Sparql.GetDataSparql(s.GetLinksSpotlight(p, 0.8, 0, "fr"));

                    extendArray(URIs, json);
                }

                tableOfURIs.put(URIs);
            } catch (Exception e) {
                e.printStackTrace();
            }

            break;
        }

        return tableOfURIs;
    }
}