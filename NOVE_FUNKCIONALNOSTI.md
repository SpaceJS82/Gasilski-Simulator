# Gasilski Simulator - Nove Funkcionalnosti

## Dodane Funkcionalnosti

### 1. Statistike Gasilske Postaje

Klik na gasilsko postajo odpre okence s statistikami:

- **Število kamionov**: Prikaz razpoložljivih in skupnih vozil
- **Zasedenost**: Odstotek vozil na misiji
- **Pogašeni požari**: Število uspešno pogašenih požarov
- **Povprečni odzivni čas**: Povprečni čas od posredovanja do gašenja

### 2. Dinamično Gašenje Požarov

Vsak požar ima zdaj:

- **Potrebno število vozil**: Glede na resnost požara (1-4 vozila)
- **Izbira števila vozil**: Uporabnik izbere koliko vozil poslje (+ in - gumbi)
- **Dinamičen čas gašenja**:
  - Osnovni čas: 30 sekund za optimalno število vozil
  - Manj vozil = počasnejše gašenje
  - Več vozil = hitrejše gašenje
  - Formula: `čas = 30 / (število_poslanih_vozil / potrebna_vozila)`

### 3. Izboljšan Požarni Dialog

Prikaže dodatne informacije:

- Potrebno število gasilskih vozil za požar
- Število razpoložljivih vozil v postaji
- Selektor za izbiro števila vozil (+ / - gumbi)
- Onemogočen gumb, če ni dovolj vozil

### 4. Upravljanje Vozil

- Gasilska postaja ima 8 vozil (nastavljivo v `station.json`)
- Vozila se "rezervirajo" ko jih pošljemo na požar
- Po pogašenem požaru se vozila vrnejo na postajo
- Statistike se posodabljajo v realnem času

## Tehnične Spremembe

### Novi Razredi:

- `FireStation.java` - Razred za gasilsko postajo s statistikami
- `StationPopupWindow.java` - UI okno za prikaz statistik postaje

### Posodobljeni Razredi:

- `FirePoint.java` - Dodani atributi za število vozil in čas gašenja
- `FirePopupWindow.java` - Dodana izbira števila vozil
- `DispatchManager.java` - Upravljanje z več vozili in dinamično gašenje
- `GameWorld.java` - Dodana podpora za klik na postajo
- `GasilskiSimulator.java` - Integracija novih funkcionalnosti

### Konfiguracijske Datoteke:

- `station.json` - Dodano polje `totalTrucks` za število vozil

## Uporaba

1. **Klik na gasilsko postajo**: Prikaže okno s statistikami
2. **Klik na požar**: Prikaže okno z informacijami in selekcijo vozil
3. **Izbira vozil**: Uporabi + in - gumbe za izbiro števila
4. **Pošlji vozila**: Klikni "PUT OUT THE FIRE!"
5. **Opazuj**: Vozila potujejo, gašenje je odvisno od števila poslanih vozil

## Primeri

- **Močan požar (3 vozila potrebna)**:
  - 1 vozilo → 90 sekund gašenja
  - 2 vozili → 45 sekund gašenja
  - 3 vozila → 30 sekund gašenja
  - 4 vozila → 22.5 sekund gašenja

- **Šibek požar (1 vozilo potrebno)**:
  - 1 vozilo → 30 sekund gašenja
  - 2 vozili → 15 sekund gašenja
