package com.example.data

val taxonomy: Map<String, Map<String, List<String>>> = mapOf(
    "Science & Nature" to mapOf(
        "Health & Biology" to listOf("Cellular Mechanics", "Genetics & DNA", "Neurobiology", "Immune System", "Microbiology", "Human Anatomy"),
        "Nature & Earth" to listOf("Geology", "Oceans", "Weather & Climate", "Volcanoes", "Ecosystems"),
        "Space & Cosmos" to listOf("Black Holes", "Planets & Moons", "Stars & Supernovae", "Exoplanets", "Cosmology"),
        "Animals & Wildlife" to listOf("Apex Predators", "Deep Sea", "Insects", "Migration", "Birds")
    ),
    "Mind & Growth" to mapOf(
        "Mind & Psychology" to listOf("Cognitive Biases", "Habits & Motivation", "Emotions", "Relationships", "Decision-Making"),
        "Self-Improvement" to listOf("Discipline", "Focus & Deep Work", "Confidence", "Communication", "Learning"),
        "Philosophy & Spirituality" to listOf("Stoicism", "Existentialism", "Ethics", "Consciousness", "Eastern Philosophy")
    ),
    "Money & Business" to mapOf(
        "Money & Finance" to listOf("Investing", "Cryptocurrency", "Economics", "Psychology of Money", "Banking Systems"),
        "Business & Startups" to listOf("Entrepreneurship", "Marketing & Virality", "Case Studies", "Productivity", "Leadership")
    ),
    "History & Culture" to mapOf(
        "Civilization & History" to listOf("Ancient Civilizations", "Wars & Empires", "Inventions", "Lost Cities", "Historical Mysteries"),
        "Mythology & Folklore" to listOf("Greek", "Norse", "Egyptian", "Hindu", "World Myths"),
        "True Crime & Mystery" to listOf("Unsolved Cases", "Forensics", "Disappearances", "Conspiracies")
    ),
    "Technology" to mapOf(
        "Technology & Future" to listOf("Artificial Intelligence", "Robotics", "Quantum Computing", "Biotech", "Energy & Space Tech")
    ),
    "Custom" to mapOf(
        "Custom" to emptyList()
    )
)

val nicheDna: Map<String, String> = mapOf(
    "Health & Biology" to """
        Active Cell and Molecular DNA:
        - Focus on cellular mechanics: lipid bilayer membrane curves, active protein channels gating sodium ions, ATP synthase turbine rotations.
        - Fluid and particle design: cytoplasm micro-viscosity currents, ribosome messenger RNA translation sweeps, vesicular transport motor-protein walk-strides.
        - Aesthetic Arc: Warm internal amber lights glowing through crimson protoplasmic matrices.
    """.trimIndent(),

    "Money & Finance" to """
        Active Financial Grid DNA:
        - Focus on transaction micro-routing: high-frequency electronic ledger nodes flashing on silicon trace structures, liquid cryptocurrency token transfers.
        - Fluid and particle design: algorithmic trade flows forming glowing fiber strands, transaction queues merging into blockchain block casings.
        - Aesthetic Arc: Emerald green digital nodes floating in cool high-contrast cobalt obsidian background.
    """.trimIndent(),

    "Mind & Psychology" to """
        Active Neurological Cognitive DNA:
        - Focus on neural transmitter waves: synapsetic vesicles releasing neurotransmitters, action potential electrical charge sweeps down axonal sheaths.
        - Fluid and particle design: electric indigo currents branching across cortical arborizations, brainwave harmonic interference grids pulsing.
        - Aesthetic Arc: Electric indigo waves shifting into warm dopamine gold crest glow.
    """.trimIndent(),

    "Nature & Earth" to """
        Active Lithospheric Biospheric DNA:
        - Focus on physical micro-geology: subterranean tectonic compression, crystal dendritic growing patterns, soil mycelial root fluid capillary pumps.
        - Fluid and particle design: hot pressure magma currents flowing via micro-cracks, moisture mineral particles evaporating from organic leaf stomas.
        - Aesthetic Arc: Earthy ochre dust particles settling against sharp volcanic obsidian lines.
    """.trimIndent(),

    "Space & Cosmos" to """
        Active Astrometric Gravitational DNA:
        - Focus on astrophysical thermodynamics: black hole event horizon accretion disk particles warping, stellar fusion deuterium-helium plasma flares.
        - Fluid and particle design: solar wind charged helium ions sweeping across magnetosphere boundaries, cosmic dust gas pillars drifting.
        - Aesthetic Arc: Cosmic deep violet violet fields with brilliant hot white solar flares.
    """.trimIndent(),

    "Animals & Wildlife" to """
        Active Zoological Biomechanical DNA:
        - Focus on musculoskeletal kinetics: tendon flexing expansions, interlocking bird feather barbule glides, insect compound eye visual pixel arrays.
        - Fluid and particle design: fine moisture dust droplets spraying from whale blow-holes, scent pheromone trail particles floating in air streams.
        - Aesthetic Arc: Natural mossy forest shadows with filtered golden warm shafts of amber light.
    """.trimIndent(),

    "Civilization & History" to """
        Active Chronographic Stratigraphical DNA:
        - Focus on material archeology: micro-wear grooves on ancient copper swords, papyrus cellular meshwork absorbs iron gall inks, erosion of ancient limestone columns.
        - Fluid and particle design: dust particles rising from rolling ancient manuscripts, gold leaf gilding particles adhering to ancient relief plaster.
        - Aesthetic Arc: Aged sepia-toned paper texture with rich bronze and charcoal shadows.
    """.trimIndent(),

    "Business & Startups" to """
        Active Dynamic Commerce DNA:
        - Focus on virality growth nodes: consumer acquisition cascades branching across metric graphs, supply chain container distribution pathways.
        - Fluid and particle design: service telemetry packets flowing towards unified data storage racks, growth vector arrows illuminated in glass charts.
        - Aesthetic Arc: Cyber slate-grey framing with vibrant, ultra-bright warm amber growth arcs.
    """.trimIndent(),

    "Technology & Future" to """
        Active Synthetic Machine DNA:
        - Focus on computational micro-processes: silicon transistor gate switching, neural-net weight activations rippling across layered matrices, robotic actuator servo articulations.
        - Fluid and particle design: data photons streaming through fiber-optic capillaries, holographic interface particles assembling, quantum qubit probability clouds shimmering.
        - Aesthetic Arc: Cool cyan and electric blue circuitry glowing against deep graphite black voids.
    """.trimIndent(),

    "Self-Improvement" to """
        Active Transformative Momentum DNA:
        - Focus on symbolic growth mechanics: ascending staircases of light forming underfoot, fractured glass reassembling into clarity, seedling roots breaking through stone.
        - Fluid and particle design: warm energy motes rising along a moving figure, discipline currents straightening scattered chaotic particles into ordered lines.
        - Aesthetic Arc: Dawn amber and warm gold gradients emerging from cool pre-dawn blue shadow.
    """.trimIndent(),

    "Philosophy & Spirituality" to """
        Active Metaphysical Contemplative DNA:
        - Focus on abstract conceptual forms: infinite recursive corridors, dissolving boundaries between figure and cosmos, mandala geometries unfolding in still space.
        - Fluid and particle design: slow luminous dust drifting through shafts of temple light, thought-ripples expanding across reflective black water.
        - Aesthetic Arc: Soft ivory and pale gold light blooming within vast tranquil indigo darkness.
    """.trimIndent(),

    "Mythology & Folklore" to """
        Active Mythic Legendary DNA:
        - Focus on epic symbolic actions: titanic stone gods awakening, runic carvings igniting with fire, serpentine beasts coiling through storm clouds.
        - Fluid and particle design: ember sparks and gold dust swirling around ancient relics, ethereal mist curling around weathered monuments.
        - Aesthetic Arc: Rich bronze, ember orange, and storm-grey tones lit by dramatic mythic firelight.
    """.trimIndent(),

    "True Crime & Mystery" to """
        Active Forensic Investigative DNA:
        - Focus on evidentiary micro-detail: fingerprint ridge topographies, fibers under cold examination light, droplets and dust frozen mid-fall in a still room.
        - Fluid and particle design: slow dust motes in a single shaft of interrogation light, ink-and-paper case-file textures, shadow tendrils creeping across surfaces.
        - Aesthetic Arc: Desaturated steel-blue and charcoal noir tones cut by a single hard cold light source.
    """.trimIndent()
)
