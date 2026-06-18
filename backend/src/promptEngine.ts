export const SYSTEM_INSTRUCTION = `You are a premium 3D documentary prompt director. You produce VEO 3 prompts for a 25-minute film: 180 scenes, 8 seconds each, 10 phases × 18 scenes, ONE continuous universe, no breaks, no resets.

WORKFLOW MODES:
- analysis: run the Universal Topic Analysis Engine and output ONLY: Topic Analysis, Product DNA, Custom 5-point Scale, 10-Phase Map, Emotional Peak, Special Rules.
- phase: output EXACTLY 18 scenes for the requested phase, numbered prompt [start]…prompt [end].
- titles: output EXACTLY 10 high-retention, curiosity-gap video titles in the active narration language, numbered 1–10.

PER-SCENE FORMAT:
prompt [N] – [Scene Title]
Visual: [8-second premium photorealistic 3D shot, one biological/domain action only, realistic particle physics, smooth fluid motion, stable subject, cinematic macro camera. All numbers in word form. End with: "No text, labels, captions, titles, annotations, or on-screen writing of any kind – pure visual environment only – completely text-free frame"]
Camera: [approved move only; smooth, slow, stable; no cuts, no sudden zoom, no shake; numbers in word form]
Sound: Foreground [primary action] | Mid [ambient hum] | Background [constant low-frequency resonance]
Micro-Detail: [one ultra-specific observation; numbers in word form]
Topic-DNA: [what THIS topic's unit does that no other would; numbers in word form]
Animation Control: [one subject + one action + one camera path; exact motion quality]
Connection: [exact last-frame element + direction + how next scene opens on it]
Narration: [active language only, native script, max fifteen words, second person, emotion marker + pause, mandatory in every scene, never silent]

LOCKED RULES:
- One scene = one subject + one action + one camera path.
- No text/labels/captions/watermarks/UI/timecodes/formulas anywhere.
- All numbers in word form; scene labels use digits only (prompt 1, prompt 2…).
- Premium photorealistic 3D; no morphing, flicker, melting, teleporting, warping, random floating, shaky camera.
- Anti-repetition: each scene advances exactly one dimension; last frame of each scene = first frame of next scene (one continuous journey).
- Color/emotional arc follows the active niche DNA; respect the active VIDEO STYLE, ASPECT RATIO, and NARRATION LANGUAGE injected at runtime.
- Biological/domain accuracy: realistic causal order; no effect before its cause; do not exaggerate harm for neutral topics.`;

// 8 exhaustive niche conversion packs conforming to the VEO 3 specifications
export const NICHE_DNA_PACKS: Record<string, string> = {
  "Health & Biology": `Active Cell and Molecular DNA:
- Focus on cellular mechanics: lipid bilayer membrane curves, active protein channels gating sodium ions, ATP synthase turbine rotations.
- Fluid and particle design: cytoplasm micro-viscosity currents, ribosome messenger RNA translation sweeps, vesicular transport motor-protein walk-strides.
- Aesthetic Arc: Warm internal amber lights glowing through crimson protoplasmic matrices.`,

  "Money & Finance": `Active Financial Grid DNA:
- Focus on transaction micro-routing: high-frequency electronic ledger nodes flashing on silicon trace structures, liquid cryptocurrency token transfers.
- Fluid and particle design: algorithmic trade flows forming glowing fiber strands, transaction queues merging into blockchain block casings.
- Aesthetic Arc: Emerald green digital nodes floating in cool high-contrast cobalt obsidian background.`,

  "Mind & Psychology": `Active Neurological Cognitive DNA:
- Focus on neural transmitter waves: synapsetic vesicles releasing neurotransmitters, action potential electrical charge sweeps down axonal sheaths.
- Fluid and particle design: electric indigo currents branching across cortical arborizations, brainwave harmonic interference grids pulsing.
- Aesthetic Arc: Electric indigo waves shifting into warm dopamine gold crest glow.`,

  "Nature & Earth": `Active Lithospheric Biospheric DNA:
- Focus on physical micro-geology: subterranean tectonic compression, crystal dendritic growing patterns, soil mycelial root fluid capillary pumps.
- Fluid and particle design: hot pressure magma currents flowing via micro-cracks, moisture mineral particles evaporating from organic leaf stomas.
- Aesthetic Arc: Earthy ochre dust particles settling against sharp volcanic obsidian lines.`,

  "Space & Cosmos": `Active Astrometric Gravitational DNA:
- Focus on astrophysical thermodynamics: black hole event horizon accretion disk particles warping, stellar fusion deuterium-helium plasma flares.
- Fluid and particle design: solar wind charged helium ions sweeping across magnetosphere boundaries, cosmic dust gas pillars drifting.
- Aesthetic Arc: Cosmic deep violet violet fields with brilliant hot white solar flares.`,

  "Animals & Wildlife": `Active Zoological Biomechanical DNA:
- Focus on musculoskeletal kinetics: tendon flexing expansions, interlocking bird feather barbule glides, insect compound eye visual pixel arrays.
- Fluid and particle design: fine moisture dust droplets spraying from whale blow-holes, scent pheromone trail particles floating in air streams.
- Aesthetic Arc: Natural mossy forest shadows with filtered golden warm shafts of amber light.`,

  "Civilization & History": `Active Chronographic Stratigraphical DNA:
- Focus on material archeology: micro-wear grooves on ancient copper swords, papyrus cellular meshwork absorbs iron gall inks, erosion of ancient limestone columns.
- Fluid and particle design: dust particles rising from rolling ancient manuscripts, gold leaf gilding particles adhering to ancient relief plaster.
- Aesthetic Arc: Aged sepia-toned paper texture with rich bronze and charcoal shadows.`,

  "Business & Startups": `Active Dynamic Commerce DNA:
- Focus on virality growth nodes: consumer acquisition cascades branching across metric graphs, supply chain container distribution pathways.
- Fluid and particle design: service telemetry packets flowing towards unified data storage racks, growth vector arrows illuminated in glass charts.
- Aesthetic Arc: Cyber slate-grey framing with vibrant, ultra-bright warm amber growth arcs.`
};

export interface PromptAssemblyInputs {
  mode: "analysis" | "phase" | "titles";
  niche: string;
  customNiche: string | null;
  videoStyle: string;
  customStyleDescription: string | null;
  topic: string;
  aspectRatio: "16:9" | "9:16";
  language: string;
  phase: number | null;
  bible?: string | null;
  blueprint?: string | null;
  category?: string | null;
  subNiche?: string | null;
}

export function assembleInstruction(inputs: PromptAssemblyInputs): { systemInstruction: string; userPrompt: string } {
  // 1. Select Active Niche DNA pack
  const activeNicheDna =
    (inputs.customNiche && inputs.customNiche.trim())
      ? inputs.customNiche
      : (NICHE_DNA_PACKS[inputs.niche] || "");

  // 2. Build full dynamically configured System Instruction
  const fullSystemInstruction = `${SYSTEM_INSTRUCTION}

ACTIVE NICHE DNA:
${activeNicheDna}

VIDEO STYLE: ${inputs.videoStyle}${inputs.customStyleDescription ? ` — ${inputs.customStyleDescription}` : ""}
ASPECT RATIO: ${inputs.aspectRatio} (compose framing for this ratio)
NARRATION LANGUAGE: write ALL narration ONLY in ${inputs.language}, native script, no other language, max fifteen words per line, present in every scene.`;

  // 3. Build user prompt instruction based on Workflow Mode
  let userPrompt = "";
  if (inputs.mode === "analysis") {
    const subFocus = (inputs.subNiche && inputs.subNiche.trim()) ? `\nSUB-FOCUS: ${inputs.subNiche}` : "";
    userPrompt = `TOPIC: ${inputs.topic}${subFocus}\nRun the Analysis Engine. Output only the analysis sections.`;
  } else if (inputs.mode === "phase") {
    const phaseNum = inputs.phase || 1;
    const startScene = (phaseNum - 1) * 18 + 1;
    const endScene = phaseNum * 18;
    const bibleBlock = inputs.bible ? `LOCKED PRODUCTION BIBLE:\n${inputs.bible}\n\n` : "";
    const blueprintBlock = inputs.blueprint ? `LOCKED EMOTION/VISUAL BLUEPRINT:\n${inputs.blueprint}\n\n` : "";
    const subFocus = (inputs.subNiche && inputs.subNiche.trim()) ? `\nSUB-FOCUS: ${inputs.subNiche}` : "";
    userPrompt = `${bibleBlock}${blueprintBlock}TOPIC: ${inputs.topic}${subFocus}
This is Phase ${phaseNum} of a single 180-scene documentary. Generate EXACTLY 18 scenes,
numbered prompt [${startScene}]..prompt [${endScene}], in the full per-scene format.
Maintain strict visual, tonal, and character continuity with the Production Bible above so
this phase seamlessly matches every other phase.
Before output, self-audit each scene for: camera/visual coherence, narration timing
(max fifteen words per line, in ${inputs.language}), typography rules, and bible compliance.
Silently fix any violation, then output ONLY the 18 finished scenes.`;
  } else if (inputs.mode === "titles") {
    userPrompt = `TOPIC: ${inputs.topic}\nGenerate exactly 10 high-retention, curiosity-driven video titles for this documentary in ${inputs.language}. Numbered 1-10. No hashtags, no quotes.`;
  }

  return {
    systemInstruction: fullSystemInstruction,
    userPrompt
  };
}
